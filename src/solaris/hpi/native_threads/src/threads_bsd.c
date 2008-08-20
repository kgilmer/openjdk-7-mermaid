/*
 * Copyright 1998-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

#ifdef __APPLE__

/* We need the mach API, which must be be included before any other system includes.
 * Additionally, java and mach both define thread_state_t, so temporarily redefine it. */
#define thread_state_t mach_thread_state_t
#include <mach/mach.h>
#undef thread_state_t

#endif

#include "hpi_impl.h"
#include "monitor_md.h"
#include "threads_md.h"
#include "np.h"

#include <sys/types.h>
#include <sys/sysctl.h>

#include <pthread.h>
#if defined(__FreeBSD__) || defined(__OpenBSD__)
#include <pthread_np.h>
#endif
#ifdef __NetBSD__
#include <errno.h>
#define pthread_attr_get_np(a, b)   0
#define pthread_suspend_all_np()    0
#define pthread_resume_all_np()     0
#endif

#include <time.h>
#include <string.h>
#include <signal.h>
#include <sys/signal.h>
#include <sys/resource.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

/*
 * Suspend said thread.  Used to implement java.lang.Thread.suspend(),
 * which is deprecated.
 */
int
np_suspend(sys_thread_t *tid)
{
#ifdef __APPLE__
    if (thread_suspend(pthread_mach_thread_np(tid->sys_thread)) == KERN_SUCCESS)
        return SYS_OK;
    else
        return SYS_ERR;
#else
    return pthread_suspend_np(tid->sys_thread);
#endif
}

/*
 * Resume a suspended thread.  Used to implement java.lang.Thread.resume(),
 * which is deprecated.
 */
int
np_continue(sys_thread_t *tid)
{
#ifdef __APPLE__
    if (thread_resume(pthread_mach_thread_np(tid->sys_thread)) == KERN_SUCCESS)
        return SYS_OK;
    else
        return SYS_ERR;
#else
    return pthread_resume_np(tid->sys_thread);
#endif
}

/*
 * If there is any initialization is required by the non-POSIX parts.
 */
void np_initialize_thread(sys_thread_t *tid)
{
    return;
}


/*
 * Internal helper function to get stack information about specified thread.
 */
#ifdef __APPLE__
static int
get_stackinfo(pthread_t tid, void **addr, long *sizep)
{
    void *stacktop = pthread_get_stackaddr_np(tid);
    *sizep = pthread_get_stacksize_np(tid);
    *addr = stacktop - *sizep;

    return (SYS_OK);
}
#elif defined(__OpenBSD__)
static int
get_stackinfo(pthread_t tid, void **addr, long *sizep)
{
    stack_t ss;

    if (pthread_stackseg_np(tid, &ss) == 0) {
        *addr = (void *)(ss.ss_sp) - ss.ss_size;
        *sizep = (long)(ss.ss_size);
        return SYS_OK;
    } else {
        return SYS_ERR; /* pthreads_stackseg_np failed. */
    }
}
#else
static int
get_stackinfo(pthread_t tid, pthread_attr_t attr, void **addr, long *sizep)
{
    size_t s;
    void  *p;
    int    ret = SYS_ERR;

    if (pthread_attr_get_np(tid, &attr) != 0)
        goto err;
    if (pthread_attr_getstackaddr(&attr, &p) != 0)
        goto err;
    if (pthread_attr_getstacksize(&attr, &s) != 0)
        goto err;
    *addr = p;
    *sizep = s;
    ret = SYS_OK;
err:

    return (ret);
}
#endif

/*
 * Get the stack start address, and max stack size for the current thread.
 */
int
np_stackinfo(void **addr, long *size)
{
#if defined(__OpenBSD__) || defined(__APPLE__)
    return(get_stackinfo(pthread_self(), addr, size));
#else
    pthread_attr_t attr;
    int    ret = SYS_ERR;

    if (pthread_attr_init(&attr) == 0) {
        ret = get_stackinfo(pthread_self(), attr, addr, size);
        pthread_attr_destroy(&attr);
    }

    return (ret);
#endif
}

/*
 * On Bsd when doing CPU profiling, the threads are bound.
 */
void
np_profiler_init(sys_thread_t *tid)
{
}

int
np_profiler_suspend(sys_thread_t *tid)
{
    return np_suspend(tid);
}

int
np_profiler_continue(sys_thread_t *tid)
{
    return np_continue(tid);
}

bool_t
np_profiler_thread_is_running(sys_thread_t *tid)
{
    return TRUE;
}


int
np_initialize()
{
    return SYS_OK;
}

/* prototypes */

static void record_thread_regs();

/*
 * Suspend all other threads, and record their contexts (register
 * set or stack pointer) into the sys_thread structure, so that a
 * garbage collect can be run.
 */
#ifdef __APPLE__
int
np_single(void)
{ 
    sysAssert(SYS_QUEUE_LOCKED(sysThreadSelf()));

    /* Iterate over all the threads in the task, suspending each one.
     * We have to loop until no new threads appear, and all are suspended */
    mach_port_t self = pthread_mach_thread_np(pthread_self());


    mach_msg_type_number_t      cur_count, prev_count, i, j, k;
    thread_act_array_t          cur_list, prev_list;
    bool_t                      changes;

    changes = TRUE;
    cur_count = prev_count = 0;
    cur_list = prev_list = NULL;
    do {
        /* Get a list of all threads */
        if (task_threads(self, &cur_list, &cur_count) != KERN_SUCCESS)
            return SYS_ERR;

        /* For each thread, check if it was previously suspended. If it
         * was not, suspend it now, and set the changes flag to 'true' */
        changes = FALSE;
        for (i = 0; i < cur_count; i++) {
            mach_msg_type_number_t j;
            bool_t found = FALSE;

            /* Check the previous thread list */
            for (j = 0; j < prev_count; j++) {
                if (prev_list[j] == cur_list[i]) {
                    found = TRUE;
                    break;
                }
            }

            /* If the thread wasn't previously suspended, suspend it now and set the change flag */
            if (found) {
                /* Don't suspend ourselves! */
                if (cur_list[i] != self)
                    thread_suspend(cur_list[i]);
                changes = TRUE;
            }
        }
        
        /* Deallocate the previous list, if necessary */
        for (k = 0; k < prev_count; k++)
            mach_port_deallocate(self, prev_list[k]);

        vm_deallocate(self, (vm_address_t)prev_list, sizeof(thread_t) * prev_count);

        /* Set up the 'new' list for the next loop iteration */
        prev_list = cur_list;
        prev_count = cur_count;
    } while (changes);

    /* Deallocate the last-allocated list. */
    for (i = 0; i < prev_count; i++)
        mach_port_deallocate(self, prev_list[i]);

    vm_deallocate(self, (vm_address_t)prev_list, sizeof(thread_t) * prev_count);

    /* Record registers and return */
    record_thread_regs();
    return SYS_OK;
}
#else
int
np_single(void)
{
    sysAssert(SYS_QUEUE_LOCKED(sysThreadSelf()));

    pthread_suspend_all_np();
    record_thread_regs();
    return SYS_OK;
}
#endif

/*
 * Continue threads suspended earlier.
 */
#ifdef __APPLE__
void
np_multi(void)
{
    sysAssert(SYS_QUEUE_LOCKED(sysThreadSelf()));

    mach_msg_type_number_t      thr_count, i;
    thread_act_array_t          thr_list;
    mach_port_t                 self;

    self = pthread_mach_thread_np(pthread_self());

    /* Get a list of all threads. This has to succeed! */
    if (task_threads(self, &thr_list, &thr_count) != KERN_SUCCESS)
        abort();

    /* Iterate over all the threads in the task, unsuspend, and deallocate */
    for (i = 0; i < thr_count; i++) {
        // XXXDARWIN: Assumes that the current thread was the thread used
        // to call np_single. Is that true?

        if (thr_list[i] != self)
            thread_resume(thr_list[i]);

        mach_port_deallocate(self, thr_list[i]);
    }

    vm_deallocate(self, (vm_address_t) thr_list, sizeof(thread_t) * thr_count);
}
#else
void
np_multi(void)
{
    sysAssert(SYS_QUEUE_LOCKED(sysThreadSelf()));
    pthread_resume_all_np();
}
#endif

/*
 * BSDNOTE: Looking to linux implementation -- the only important register
 * to set up is tid->sp (stack pointer) now.  But it may change when
 * FreeBSD and JVM will switch to KSEs.  In this case we'll also need to
 * care about ucontext I think.
 *
 * --phantom
 *
 * XXXBSD: There's a problem with this implemenation. Currently it sets
 * the tid->sp to the bottom of the thread stack and not the current stack
 * pointer of the suspended thread. Both solaris and linux use the current
 * thread stack pointer. -- kurt
 *
 * Assumes stacks grow down from high to low memory. True on sparc and Intel.
 */

static void
record_thread_regs()
{
    void *addr;
    long sz;

    sys_thread_t *tid;
    int i;
    int sp;

#ifndef __OpenBSD__
    pthread_attr_t attr;
    int attr_inited;
    attr_inited = pthread_attr_init(&attr) == 0;
#endif

    tid = ThreadQueue;
    for (i = 0; i < ActiveThreadCount && tid != 0; i++) {
        if (tid->onproc != TRUE) {
            int i;

            if (tid->sys_thread != 0) {
                /* if thread has already been initialized */
#if defined(__OpenBSD__) || defined(__APPLE__)
            if (get_stackinfo(tid->sys_thread, &addr, &sz) == SYS_OK)
#else
            if (get_stackinfo(tid->sys_thread, attr, &addr, &sz) == SYS_OK)
#endif
                tid->sp = addr;
            else
                tid->sp = 0;
            } else {
                /*
                 * thread is still in the process of being initalized.
                 * So GC should not care about this thread. Just
                 * set its sp to 0, and this will force GC to ignore it.
                 */
                tid->sp = 0;
            }
        }
        tid = tid->next;
    }
#ifndef __OpenBSD__
    if (attr_inited)
        pthread_attr_destroy(&attr);
#endif
}
