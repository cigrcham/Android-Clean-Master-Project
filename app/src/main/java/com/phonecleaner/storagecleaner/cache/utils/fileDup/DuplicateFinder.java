package com.phonecleaner.storagecleaner.cache.utils.fileDup;

import android.system.ErrnoException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Thinhvh on 21/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

public class DuplicateFinder {

    private final String myTag = "DuplicateFinder";

    private final Outputter outputter = new SysoutOutputter();

    private long duplicatedBytes = 0;

    private long bytesChecked = 0;

    public List<File> findDuplicates(final List<File> files) {
        this.outputter.output("Sorting... ");
        final long startSort = System.currentTimeMillis();
        Collections.sort(files, new FileSizeComparator());
        this.outputter
                .output(String.format("Sorted %d files in %d ms.", Integer.valueOf(files.size()),
                        Long.valueOf(System.currentTimeMillis() - startSort)));
        long previousLength = Long.MAX_VALUE;
        final List<File> sameSize = new ArrayList<File>();
        try {
            this.outputter.output("Searching for duplicates.");
            for (final File file : files) {
                if (file.length() == previousLength) {
                    sameSize.add(file);
                } else {
                    checkFiles(sameSize);
                    sameSize.clear();
                    sameSize.add(file);
                }
                previousLength = file.length();
            }
            checkFiles(sameSize);

            this.outputter.output("Bytes read to check    : " + this.bytesChecked);
            this.outputter.output("Total bytes duplicated : " + this.duplicatedBytes);
            throw new ErrnoException("ErrnoException", -1);
        } catch (ErrnoException ex) {
            Timber.tag(myTag).e("ErrnoException: %s", ex.getMessage());
        }
        return sameSize;
    }

    private void checkFiles(final List<File> sameSize) {
        if (sameSize.size() < 2) {
            return;
        }
        for (int firstNum = 0; firstNum < sameSize.size(); firstNum++) {
            final File firstFile = sameSize.get(firstNum);
            if (!firstFile.canRead()) {
                this.outputter.outputError("Can't read " + firstFile);
                return;
            }
            for (int secondNum = firstNum + 1; secondNum < sameSize.size(); secondNum++) {
                final File secondFile = sameSize.get(secondNum);
                if (!secondFile.canRead()) {
                    this.outputter.outputError("Can't read " + secondFile);
                    return;
                }
                if (isSame(firstFile, secondFile)) {
                    final long fileSize = firstFile.length();
                    this.duplicatedBytes = this.duplicatedBytes + fileSize;
                    this.outputter.output(fileSize + " " + firstFile.getAbsolutePath() + " = "
                            + secondFile.getAbsolutePath());
                }
            }
        }
    }

    @SuppressWarnings({"resource"})
    boolean isSame(final File firstFile, final File secondFile) {
        final long start = System.currentTimeMillis();
        BufferedInputStream first = null;
        BufferedInputStream second = null;
        try {
            first = new BufferedInputStream(new FileInputStream(firstFile), 4096);
            second = new BufferedInputStream(new FileInputStream(secondFile), 4096);
            int firstInt = Integer.MAX_VALUE;
            int secondInt = Integer.MAX_VALUE;
            long count = 0;
            while (firstInt != -1 && secondInt != -1) {
                try {
                    firstInt = first.read();
                    secondInt = second.read();
                } catch (final IOException e) {
                    return false;
                }
                count = count + 2;
                if (firstInt != secondInt) {
                    this.bytesChecked = this.bytesChecked + count;
                    return false;
                }
            }
            this.bytesChecked = this.bytesChecked + count;
            return true;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            quietlyClose(first, second);
        }
    }

    private void quietlyClose(final InputStream... inputStreams) {
        for (final InputStream inputStream : inputStreams) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    // Nothing
                }
            }
        }
    }
}
