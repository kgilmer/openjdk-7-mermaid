/* *********************************************************************
 *
 * Sun elects to have this file available under and governed by the
 * Mozilla Public License Version 1.1 ("MPL") (see
 * http://www.mozilla.org/MPL/ for full license text). For the avoidance
 * of doubt and subject to the following, Sun also elects to allow
 * licensees to use this file under the MPL, the GNU General Public
 * License version 2 only or the Lesser General Public License version
 * 2.1 only. Any references to the "GNU General Public License version 2
 * or later" or "GPL" in the following shall be construed to mean the
 * GNU General Public License version 2 only. Any references to the "GNU
 * Lesser General Public License version 2.1 or later" or "LGPL" in the
 * following shall be construed to mean the GNU Lesser General Public
 * License version 2.1 only. However, the following notice accompanied
 * the original version of this file:
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Netscape security libraries.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1994-2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Dr Vipul Gupta <vipul.gupta@sun.com> and
 *   Douglas Stebila <douglas@stebila.ca>, Sun Microsystems Laboratories
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 *********************************************************************** */
/*
 * Copyright 2007 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

#ifndef _ECC_IMPL_H
#define _ECC_IMPL_H

#pragma ident   "%Z%%M% %I%     %E% SMI"

#ifdef __cplusplus
extern "C" {
#endif

#include <sys/types.h>
#include "ecl-exp.h"

/*
 * Multi-platform definitions
 */
#ifdef __linux__
#define B_FALSE FALSE
#define B_TRUE TRUE
typedef unsigned char uint8_t;
typedef unsigned long ulong_t;
typedef enum { B_FALSE, B_TRUE } boolean_t;
#endif /* __linux__ */

#ifdef _ALLBSD_SOURCE
#include <stdint.h>
#define B_FALSE FALSE
#define B_TRUE TRUE
typedef unsigned long ulong_t;
typedef enum boolean { B_FALSE, B_TRUE } boolean_t;
#endif /* _ALLBSD_SOURCE */

#ifdef _WIN32
typedef unsigned char uint8_t;
typedef unsigned long ulong_t;
typedef enum boolean { B_FALSE, B_TRUE } boolean_t;
#endif /* _WIN32 */

#ifndef _KERNEL
#include <stdlib.h>
#endif  /* _KERNEL */

#define EC_MAX_DIGEST_LEN 1024  /* max digest that can be signed */
#define EC_MAX_POINT_LEN 145    /* max len of DER encoded Q */
#define EC_MAX_VALUE_LEN 72     /* max len of ANSI X9.62 private value d */
#define EC_MAX_SIG_LEN 144      /* max signature len for supported curves */
#define EC_MIN_KEY_LEN  112     /* min key length in bits */
#define EC_MAX_KEY_LEN  571     /* max key length in bits */
#define EC_MAX_OID_LEN 10       /* max length of OID buffer */

/*
 * Various structures and definitions from NSS are here.
 */

#ifdef _KERNEL
#define PORT_ArenaAlloc(a, n, f)        kmem_alloc((n), (f))
#define PORT_ArenaZAlloc(a, n, f)       kmem_zalloc((n), (f))
#define PORT_ArenaGrow(a, b, c, d)      NULL
#define PORT_ZAlloc(n, f)               kmem_zalloc((n), (f))
#define PORT_Alloc(n, f)                kmem_alloc((n), (f))
#else
#define PORT_ArenaAlloc(a, n, f)        malloc((n))
#define PORT_ArenaZAlloc(a, n, f)       calloc(1, (n))
#define PORT_ArenaGrow(a, b, c, d)      NULL
#define PORT_ZAlloc(n, f)               calloc(1, (n))
#define PORT_Alloc(n, f)                malloc((n))
#endif

#define PORT_NewArena(b)                (char *)12345
#define PORT_ArenaMark(a)               NULL
#define PORT_ArenaUnmark(a, b)
#define PORT_ArenaRelease(a, m)
#define PORT_FreeArena(a, b)
#define PORT_Strlen(s)                  strlen((s))
#define PORT_SetError(e)

#define PRBool                          boolean_t
#define PR_TRUE                         B_TRUE
#define PR_FALSE                        B_FALSE

#ifdef _KERNEL
#define PORT_Assert                     ASSERT
#define PORT_Memcpy(t, f, l)            bcopy((f), (t), (l))
#else
#define PORT_Assert                     assert
#define PORT_Memcpy(t, f, l)            memcpy((t), (f), (l))
#endif

#define CHECK_OK(func) if (func == NULL) goto cleanup
#define CHECK_SEC_OK(func) if (SECSuccess != (rv = func)) goto cleanup

typedef enum {
        siBuffer = 0,
        siClearDataBuffer = 1,
        siCipherDataBuffer = 2,
        siDERCertBuffer = 3,
        siEncodedCertBuffer = 4,
        siDERNameBuffer = 5,
        siEncodedNameBuffer = 6,
        siAsciiNameString = 7,
        siAsciiString = 8,
        siDEROID = 9,
        siUnsignedInteger = 10,
        siUTCTime = 11,
        siGeneralizedTime = 12
} SECItemType;

typedef struct SECItemStr SECItem;

struct SECItemStr {
        SECItemType type;
        unsigned char *data;
        unsigned int len;
};

typedef SECItem SECKEYECParams;

typedef enum { ec_params_explicit,
               ec_params_named
} ECParamsType;

typedef enum { ec_field_GFp = 1,
               ec_field_GF2m
} ECFieldType;

struct ECFieldIDStr {
    int         size;   /* field size in bits */
    ECFieldType type;
    union {
        SECItem  prime; /* prime p for (GFp) */
        SECItem  poly;  /* irreducible binary polynomial for (GF2m) */
    } u;
    int         k1;     /* first coefficient of pentanomial or
                         * the only coefficient of trinomial
                         */
    int         k2;     /* two remaining coefficients of pentanomial */
    int         k3;
};
typedef struct ECFieldIDStr ECFieldID;

struct ECCurveStr {
        SECItem a;      /* contains octet stream encoding of
                         * field element (X9.62 section 4.3.3)
                         */
        SECItem b;
        SECItem seed;
};
typedef struct ECCurveStr ECCurve;

typedef void PRArenaPool;

struct ECParamsStr {
    PRArenaPool * arena;
    ECParamsType  type;
    ECFieldID     fieldID;
    ECCurve       curve;
    SECItem       base;
    SECItem       order;
    int           cofactor;
    SECItem       DEREncoding;
    ECCurveName   name;
    SECItem       curveOID;
};
typedef struct ECParamsStr ECParams;

struct ECPublicKeyStr {
    ECParams ecParams;
    SECItem publicValue;   /* elliptic curve point encoded as
                            * octet stream.
                            */
};
typedef struct ECPublicKeyStr ECPublicKey;

struct ECPrivateKeyStr {
    ECParams ecParams;
    SECItem publicValue;   /* encoded ec point */
    SECItem privateValue;  /* private big integer */
    SECItem version;       /* As per SEC 1, Appendix C, Section C.4 */
};
typedef struct ECPrivateKeyStr ECPrivateKey;

typedef enum _SECStatus {
        SECBufferTooSmall = -3,
        SECWouldBlock = -2,
        SECFailure = -1,
        SECSuccess = 0
} SECStatus;

#ifdef _KERNEL
#define RNG_GenerateGlobalRandomBytes(p,l) ecc_knzero_random_generator((p), (l))
#else
/*
 This function is no longer required because the random bytes are now
 supplied by the caller. Force a failure.
VR
#define RNG_GenerateGlobalRandomBytes(p,l) SECFailure
*/
#define RNG_GenerateGlobalRandomBytes(p,l) SECSuccess
#endif
#define CHECK_MPI_OK(func) if (MP_OKAY > (err = func)) goto cleanup
#define MP_TO_SEC_ERROR(err)

#define SECITEM_TO_MPINT(it, mp)                                        \
        CHECK_MPI_OK(mp_read_unsigned_octets((mp), (it).data, (it).len))

extern int ecc_knzero_random_generator(uint8_t *, size_t);
extern ulong_t soft_nzero_random_generator(uint8_t *, ulong_t);

extern SECStatus EC_DecodeParams(const SECItem *, ECParams **, int);
extern SECItem * SECITEM_AllocItem(PRArenaPool *, SECItem *, unsigned int, int);
extern SECStatus SECITEM_CopyItem(PRArenaPool *, SECItem *, const SECItem *,
    int);
extern void SECITEM_FreeItem(SECItem *, boolean_t);
extern SECStatus EC_NewKey(ECParams *ecParams, ECPrivateKey **privKey, const unsigned char* random, int randomlen, int);
extern SECStatus EC_NewKeyFromSeed(ECParams *ecParams, ECPrivateKey **privKey,
    const unsigned char *seed, int seedlen, int kmflag);
extern SECStatus ECDSA_SignDigest(ECPrivateKey *, SECItem *, const SECItem *,
    const unsigned char* randon, int randomlen, int);
extern SECStatus ECDSA_SignDigestWithSeed(ECPrivateKey *, SECItem *,
    const SECItem *, const unsigned char *seed, int seedlen, int kmflag);
extern SECStatus ECDSA_VerifyDigest(ECPublicKey *, const SECItem *,
    const SECItem *, int);
extern SECStatus ECDH_Derive(SECItem *, ECParams *, SECItem *, boolean_t,
    SECItem *, int);

#ifdef  __cplusplus
}
#endif

#endif /* _ECC_IMPL_H */
