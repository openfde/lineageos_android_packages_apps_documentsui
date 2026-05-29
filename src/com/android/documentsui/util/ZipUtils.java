package com.android.documentsui.util;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void zipFiles(List<File> sourceFiles, File zipFile) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(zipFile)));

        try {
            for (File file : sourceFiles) {
                zipFileRecursive(file, file.getName(), zos);
            }
        } finally {
            zos.close();
        }
    }

    private static void zipFileRecursive(File file, String entryName, ZipOutputStream zos) throws IOException {
        if (file.isHidden()) return;

        if (file.isDirectory()) {
            File[] children = file.listFiles();

            if (children == null || children.length == 0) {
                // 空目录
                zos.putNextEntry(new ZipEntry(entryName + "/"));
                zos.closeEntry();
            } else {
                for (File child : children) {
                    zipFileRecursive(child, entryName + "/" + child.getName(), zos);
                }
            }
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        try {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            zos.closeEntry();
        } finally {
            fis.close();
        }
    }


    public static void unzip(File zipFile, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));

        try {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());

                // ⚠️ 防止 Zip Slip 漏洞
                if (!outFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath())) {
                    throw new SecurityException("Zip entry is outside target dir: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(outFile);
                    try {
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                    } finally {
                        fos.close();
                    }
                }

                zis.closeEntry();
            }
        } finally {
            zis.close();
        }
    }
}
