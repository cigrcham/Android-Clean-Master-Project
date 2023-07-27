package com.phonecleaner.storagecleaner.cache.utils.fileDup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Thinhvh on 21/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

public class FileFinder {
    private final Outputter outputter = new SysoutOutputter();
    private final List<File> files = new ArrayList<File>();

    public void findFiles(final File dir, final int minBytes) {
        if (!dir.isDirectory()) {
            this.outputter.outputError(dir + " is not a directory!");
            return;
        }
        final File[] entries = dir.listFiles();
        if (entries == null) {
            this.outputter.outputError(dir + " entries is null!");
            return;
        }

        for (final File entry : entries) {
            try {
                if (!isSymlink(entry)) {
                    if (entry.isFile()) {
                        if (entry.length() >= minBytes) {
                            this.files.add(entry);
                        }
                    } else if (entry.isDirectory()) {
                        if (entry.canRead()) {
                            findFiles(entry, minBytes);
                        } else {
                            this.outputter.outputError("Can't read directory " + entry);
                        }
                    } else {
                        this.outputter.outputError("Skipping " + entry
                                + " as it's not a file or dir");
                    }
                } else {
                    this.outputter.outputError("Skipping " + entry + " as it's a symlink");
                }
            } catch (final NullPointerException e) {
                this.outputter.outputError("Null pointer exception processing " + entry + " in "
                        + dir + "\n" + exceptionToString(e));
            }
        }
    }


    private String exceptionToString(final Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private boolean isSymlink(final File file) {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        try {
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                final File canonDir = file.getParentFile().getCanonicalFile();
                canon = new File(canonDir, file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getFiles() {
        return this.files;
    }
}

class FileSizeComparator implements Comparator<File> {

    @Override
    public int compare(final File first, final File second) {
        return (int) (second.length() - first.length());
    }
}