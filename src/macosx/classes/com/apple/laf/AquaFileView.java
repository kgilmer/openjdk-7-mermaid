/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.apple.laf;

import java.io.*;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.filechooser.FileView;

class AquaFileView extends FileView {
    private static final boolean DEBUG = false;
    
    private static final int UNINITALIZED_LS_INFO = -1;
    
    static final int itemInfoIsPackage          = 0x00000002; /* Packaged directory */
    static final int itemInfoIsApplication      = 0x00000004; /* Single-file or packaged application */
    static final int itemInfoIsAliasFile        = 0x00000010; /* Alias file (includes symlinks) */
    static final int itemInfoIsSymlink          = 0x00000020; /* Symlink */
    
    static {
        java.security.AccessController.doPrivileged((PrivilegedAction<?>)new sun.security.action.LoadLibraryAction("laf"));
    }
    
    private static native String getNativeMachineName();
    private static native String getNativeDisplayName(final byte[] pathBytes, final boolean isDirectory);
    private static native int getNativeLSInfo(final byte[] pathBytes, final boolean isDirectory);
    private static native String getNativePathForResolvedAlias(final byte[] absolutePath, final boolean isDirectory);
    
    static String machineName = null;
    private static String getMachineName() {
        if (machineName == null) {
            machineName = getNativeMachineName();
        }
        return machineName;
    }
    
    static class FileInfo {
        final boolean isDirectory;
        final String absolutePath;
        byte[] pathBytes;
        
        String displayName;
        AquaIcon.CachingScalingIcon icon;
        int launchServicesInfo = UNINITALIZED_LS_INFO;
        
        FileInfo(final File file){
            isDirectory = file.isDirectory();
            absolutePath = file.getAbsolutePath();
            try {
                pathBytes = absolutePath.getBytes("UTF-8");
            } catch (final UnsupportedEncodingException e) { 
                pathBytes = new byte[0];
            }
        }
    }

    final int MAX_CACHED_ENTRIES = 256;
    protected final Map<File, FileInfo> cache = new LinkedHashMap<File, FileInfo>(){
        protected boolean removeEldestEntry(final Entry<File, FileInfo> eldest) {
            return size() > MAX_CACHED_ENTRIES;
        }
    };
    
    FileInfo getFileInfoFor(final File file) {
        final FileInfo info = cache.get(file);
        if (info != null) return info;
        final FileInfo newInfo = new FileInfo(file);
        cache.put(file, newInfo);
        return newInfo;
    }
    

    final AquaFileChooserUI fFileChooserUI;
    public AquaFileView(final AquaFileChooserUI fileChooserUI) {
        fFileChooserUI = fileChooserUI;
    }

    String _directoryDescriptionText() {
        return fFileChooserUI.directoryDescriptionText;
    }

    String _fileDescriptionText() {
        return fFileChooserUI.fileDescriptionText;
    }

    boolean _packageIsTraversable() {
        return fFileChooserUI.fPackageIsTraversable == AquaFileChooserUI.kOpenAlways;
    }

    boolean _applicationIsTraversable() {
        return fFileChooserUI.fApplicationIsTraversable == AquaFileChooserUI.kOpenAlways;
    }

    public String getName(final File f) {
        final FileInfo info = getFileInfoFor(f);
        if (info.displayName != null) return info.displayName;

        final String nativeDisplayName = getNativeDisplayName(info.pathBytes, info.isDirectory);
        if (nativeDisplayName != null) {
            info.displayName = nativeDisplayName;
            return nativeDisplayName;
        }
        
        final String displayName = f.getName();
        if (f.isDirectory() && fFileChooserUI.getFileChooser().getFileSystemView().isRoot(f)) {
            final String localMachineName = getMachineName();
            info.displayName = localMachineName;
            return localMachineName;
        }

        info.displayName = displayName;
        return displayName;
    }

    public String getDescription(final File f) {
        return f.getName();
    }

    public String getTypeDescription(final File f) {
        if (f.isDirectory()) return _directoryDescriptionText();
        return _fileDescriptionText();
    }
    
    public Icon getIcon(final File f) {
        final FileInfo info = getFileInfoFor(f);
        if (info.icon != null) return info.icon;
        
        if (f == null) {
            info.icon = AquaIcon.SystemIcon.documentIcon;
        } else {
            // Look for the document's icon
            info.icon = new AquaIcon.FileIcon(f);
            if (!info.icon.hasIconRef()) {
                // Fall back on the default icons
                if (f.isDirectory()) {
                    if (fFileChooserUI.getFileChooser().getFileSystemView().isRoot(f)) {
                        info.icon = AquaIcon.SystemIcon.computerIcon;
                    } else if (f.getParent() == null || f.getParent().equals("/")) {
                        info.icon = AquaIcon.SystemIcon.hardDriveIcon;
                    } else {
                        info.icon = AquaIcon.SystemIcon.folderIcon;
                    }
                } else {
                    info.icon = AquaIcon.SystemIcon.documentIcon;
                }
            }
        }
        
        return info.icon;
    }

    // aliases are traversable though they aren't directories
    public Boolean isTraversable(final File f) {
        if (f.isDirectory()) {
            // Doesn't matter if it's a package or app, because they're traversable
            if (_packageIsTraversable() && _applicationIsTraversable()) {
                return Boolean.TRUE;
            } else if (!_packageIsTraversable() && !_applicationIsTraversable()) {
                if (isPackage(f) || isApplication(f)) return Boolean.FALSE;
            } else if (!_applicationIsTraversable()) {
                if (isApplication(f)) return Boolean.FALSE;
            } else if (!_packageIsTraversable()) {
                // [3101730] All applications are packages, but not all packages are applications.
                if (isPackage(f) && !isApplication(f)) return Boolean.FALSE;
            }
            
            // We're allowed to traverse it
            return Boolean.TRUE;
        }
        
        if (isAlias(f)) {
            final File realFile = resolveAlias(f);
            return realFile.isDirectory() ? Boolean.TRUE : Boolean.FALSE;
        }
        
        return Boolean.FALSE;
    }

    int getLSInfoFor(final File f) {
        final FileInfo info = getFileInfoFor(f);
        
        if (info.launchServicesInfo == UNINITALIZED_LS_INFO) {
            info.launchServicesInfo = getNativeLSInfo(info.pathBytes, info.isDirectory);
        }
        
        return info.launchServicesInfo;
    }
    
    boolean isAlias(final File f) {
        final int lsInfo = getLSInfoFor(f);
        return ((lsInfo & itemInfoIsAliasFile) != 0) && ((lsInfo & itemInfoIsSymlink) == 0);
    }
    
    boolean isApplication(final File f) {
        return (getLSInfoFor(f) & itemInfoIsApplication) != 0;
    }
    
    boolean isPackage(final File f) {
        return (getLSInfoFor(f) & itemInfoIsPackage) != 0;
    }

    /**
     * Things that need to be handled:
     * -Change getFSRef to use CFURLRef instead of FSPathMakeRef
     * -Use the HFS-style path from CFURLRef in resolveAlias() to avoid
     *      path length limitations
     * -In resolveAlias(), simply resolve immediately if this is an alias
     */

    /**
     * Returns the actual file represented by this object.  This will
     * resolve any aliases in the path, including this file if it is an
     * alias.  No alias resolution requiring user interaction (e.g.
     * mounting servers) will occur.  Note that aliases to servers may
     * take a significant amount of time to resolve.  This method
     * currently does not have any provisions for a more fine-grained
     * timeout for alias resolution beyond that used by the system.
     *
     * In the event of a path that does not contain any aliases, or if the file
     *  does not exist, this method will return the file that was passed in.
     *    @return    The canonical path to the file
     *    @throws    IOException    If an I/O error occurs while attempting to
     *                            construct the path
     */
    File resolveAlias(final File mFile) {
        // If the file exists and is not an alias, there aren't
        // any aliases along its path, so the standard version
        // of getCanonicalPath() will work.
        if (mFile.exists() && !isAlias(mFile)) {
            if (DEBUG) System.out.println("not an alias");
            return mFile;
        }

        // If it doesn't exist, either there's an alias in the
        // path or this is an alias.  Traverse the path and
        // resolve all aliases in it.
        final LinkedList<String> components = getPathComponents(mFile);
        if (components == null) {
            if (DEBUG) System.out.println("getPathComponents is null ");
            return mFile;
        }
        
        File file = new File("/");
        for (final String nextComponent : components) {
            file = new File(file, nextComponent);
            final FileInfo info = getFileInfoFor(file);

            // If any point along the way doesn't exist,
            // just return the file.
            if (!file.exists()) { return mFile; }
            
            if (isAlias(file)) {
                // Resolve it!
                final String path = getNativePathForResolvedAlias(info.pathBytes, info.isDirectory);

                // <rdar://problem/3582601> If the alias doesn't resolve (on a non-existent volume, for example)
                // just return the file.
                if (path == null) return mFile;

                file = new File(path);
            }
        }
        
        return file;
    }

    /**
     * Returns a linked list of Strings consisting of the components of
     * the path of this file, in order, including the filename as the
     * last element.  The first element in the list will be the first
     * directory in the path, or "".
     *    @return A linked list of the components of this file's path
     */
    private static LinkedList<String> getPathComponents(final File mFile) {
        final LinkedList<String> componentList = new LinkedList<String>();
        String parent;

        File file = new File(mFile.getAbsolutePath());
        componentList.add(0, file.getName());
        while ((parent = file.getParent()) != null) {
            file = new File(parent);
            componentList.add(0, file.getName());
        }
        return componentList;
    }
}
