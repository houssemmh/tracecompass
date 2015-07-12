/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

/**
 * This file defines all the known event and field names for LTTng 2.0 kernel
 * traces.
 *
 * Once again, these should not be externalized, since they need to match
 * exactly what the tracer outputs. If you want to localize them in a view, you
 * should do a mapping in the viewer itself.
 *
 * @author Houssem Daoud
 */
@SuppressWarnings({"javadoc", "nls"})
public interface LttngStrings {

    /* System call names */
    static final String LTTNG_STATEDUMP_FILE_DESCRIPTOR = "lttng_statedump_file_descriptor";
    static final String SYSCALL_PREFIX = "syscall_entry";
    static final String SYSTEM_OPEN = "syscall_entry_open";
    static final String DO_SYS_OPEN = "do_sys_open";
    static final String SYSTEM_CLOSE = "syscall_entry_close";
    static final String EXIT_SYSCALL = "syscall_exit";
    static final String SYSTEM_READ = "syscall_entry_read";
    static final String SYSTEM_READV = "syscall_entry_readv";
    static final String SYSTEM_PREAD = "syscall_entry_pread";
    static final String SYSTEM_PREADV = "syscall_entry_preadv";
    static final String SYSTEM_WRITE = "syscall_entry_write";
    static final String SYSTEM_WRITEV = "syscall_entry_writev";
    static final String SYSTEM_PWRITE = "syscall_entry_pwrite";
    static final String SYSTEM_PWRITEV = "syscall_entry_pwritev";
    static final String SYSTEM_IOCTL = "syscall_entry_ioctl";
    static final String SYSTEM_MMAP_PGOFF = "syscall_entry_mmap_pgoff";
    static final String SYSTEM_LLSEEK = "syscall_entry_llseek";
    static final String SYSTEM_DUP2 = "syscall_entry_dup2";

    static final String SYSTEM_FSTAT= "syscall_entry_fstat";
    static final String SYSTEM_FSTAT64= "syscall_entry_fstat64";
    static final String SYSTEM_LSTAT= "syscall_entry_stat";
    static final String SYSTEM_LSTAT64= "syscall_entry_lstat64";
    static final String SYSTEM_STAT= "syscall_entry_stat";
    static final String SYSTEM_STAT64= "syscall_entry_stat64";
    static final String BLOCK_RQ_INSERT= "block_rq_insert";
    static final String BLOCK_RQ_ISSUE= "block_rq_issue";
    static final String BLOCK_RQ_MERGE= "block_rq_merge";
    static final String BLOCK_RQ_COMPLETE= "block_rq_complete";
    static final String BLOCK_BIO_REMAP= "block_bio_remap";
    static final String BLOCK_GETRQ= "block_getrq";
    static final String LTTNG_STATEDUMP_BLOCK_DEVICE= "lttng_statedump_block_device";
    static final String ASYNC_READAHEAD = "async_readahead_begin";
    static final String ASYNC_READAHEAD_END = "async_readahead_end";
    static final String SYNC_READAHEAD = "sync_readahead_begin";
    static final String SYNC_READAHEAD_END = "sync_readahead_end";
    static final String READ_PAGE_BEGIN = "read_page_begin";
    static final String READ_PAGE_END = "read_page_end";
    static final String CACHE_HIT= "block_cache_hit";
    static final String FILE_READ_END= "file_read_end";
    static final String SCHED_SWITCH = "sched_switch";
    static final String BLOCK_BIO_BACKMERGE = "block_bio_backmerge";
    static final String BLOCK_BIO_FRONTMERGE = "block_bio_frontmerge";
    /* Field names */
    static final String FILENAME = "filename";
    static final String CONTEXT_PROCNAME = "context._procname";
    static final String CONTEXT_TID="context._tid";
    static final String CONTEXT_PID="context._pid";
    static final String FD = "fd";
    static final String RETURN = "ret";
    static final String PID="pid";
    static final String TID="tid";
    static final String OLDFD="oldfd";
    static final String NEWFD="newfd";
    static final String COMM="comm";
    static final String RWBS="rwbs";
    static final String DISKNAME="diskname";
    static final String DEV="dev";
    static final String OLD_DEV="old_dev";
    static final String INODE="ino";
    static final String NEXT_TID="next_tid";
    static final String PREV_TID="prev_tid";
    static final String SECTOR="sector";
    static final String NR_SECTOR="nr_sector";
    static final String INDEX="index";

    }

