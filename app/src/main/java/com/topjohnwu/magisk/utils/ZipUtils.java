package com.topjohnwu.magisk.utils;

import android.content.res.AssetManager;

import com.topjohnwu.crypto.JarMap;
import com.topjohnwu.crypto.SignAPK;
import com.topjohnwu.magisk.MagiskManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ZipUtils {
    // File name in assets
    public static final String PUBLIC_KEY_NAME = "public.certificate.x509.pem";
    public static final String PRIVATE_KEY_NAME = "private.key.pk8";

    static {
        System.loadLibrary("zipadjust");
    }

    public native static void zipAdjust(String filenameIn, String filenameOut);

    public static void unzip(File zip, File folder, String path, boolean junkPath) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(zip));
        unzip(in, folder, path, junkPath);
        in.close();
    }

    public static void unzip(InputStream zip, File folder, String path, boolean junkPath) throws Exception {
        try {
            JarInputStream zipfile = new JarInputStream(zip);
            JarEntry entry;
            while ((entry = zipfile.getNextJarEntry()) != null) {
                if (!entry.getName().startsWith(path) || entry.isDirectory()){
                    // Ignore directories, only create files
                    continue;
                }
                String name;
                if (junkPath) {
                    name = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                } else {
                    name = entry.getName();
                }
                File dest = new File(folder, name);
                dest.getParentFile().mkdirs();
                try (FileOutputStream out = new FileOutputStream(dest)) {
                    Utils.inToOut(zipfile, out);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void signZip(InputStream is, File output, boolean minSign) throws Exception {
        signZip(new JarMap(is, false), output, minSign);
    }

    public static void signZip(File input, File output, boolean minSign) throws Exception {
        signZip(new JarMap(input, false), output, minSign);
    }

    public static void signZip(JarMap input, File output, boolean minSign) throws Exception {
        AssetManager assets = MagiskManager.get().getAssets();
        SignAPK.signZip(
                assets.open(PUBLIC_KEY_NAME), assets.open(PRIVATE_KEY_NAME),
                input, output, minSign);
    }
}