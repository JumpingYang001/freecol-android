/**
 *  Copyright (C) 2012   The FreeCol-Android Team
 *
 *  This file is part of FreeCol-Android.
 *
 *  FreeCol-Android is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol-Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.freecolandroid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.freecolandroid.repackaged.java.awt.GraphicsConfiguration;
import org.freecolandroid.repackaged.javax.swing.SwingUtilities;
import org.freecolandroid.ui.game.GameFragment;
import org.freecolandroid.ui.menu.GameMenuFragment;

import net.sf.freecol.FreeCol;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static long ERROR = -1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PackageManager m = getPackageManager();
        String internalPath = getPackageName();
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        try {
            PackageInfo p = m.getPackageInfo(internalPath, 0);
            internalPath = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getApplicationContext(),"Error Package name not founda!", Toast.LENGTH_SHORT).show();
        }
        String dataHome = null;
        if(getAvailableInternalMemorySize()>40)
        {
            dataHome = internalPath;
            Log.d("dataHome", "set internal path"+internalPath);
            //Toast.makeText(getApplicationContext(),"set internal path"+internalPath, Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(externalMemoryAvailable())
            {
                if(getAvailableExternalMemorySize()>40)
                {
                    dataHome = externalPath;
                    Log.d("dataHome", "set external path"+externalPath);
                    //Toast.makeText(getApplicationContext(),"set external path"+externalPath, Toast.LENGTH_SHORT).show();
                }
            }
        }

        if(dataHome==null)
        {
            Log.d("dataHome", "内部存储和外部存储都不满足空余40M的要求，程序无法继续运行!");
            Toast.makeText(getApplicationContext(),"内部存储和外部存储都不满足空余40M的要求，程序无法继续运行!", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            Log.d("dataHome", "内部存储和外部存储:"+dataHome);
        }

        File freecolFolder = new File(dataHome+ "/freecol/data");
        if(freecolFolder.exists())
        {
            Log.d("freecolFolder", "freecolFolder exist!");
        }
        else {
            Log.d("freecolFolder", "freecolFolder extracting from Assets!");
            try {
                UnzipFromAssets("freecol.zip", dataHome);
            } catch (Exception e) {
                Log.e("UnzipFromAssets error", e.getMessage());
                //Toast.makeText(getApplicationContext(),"UnzipFromAssets error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FreeCol.freeColHome = dataHome;
//        File homedir = new File(dataHome);
//        for (File dir : homedir.listFiles()) {
//            Log.d("Test","file:"+dir.getPath());
//            for (File dir2 : dir.listFiles()) {
//                Log.d("Test2","file:"+dir2.getPath());
//            }
//        }

        try {
            // Launch the game
            Log.d("dataHome", "Launch the game");
            //Toast.makeText(getApplicationContext(), "Launch the game", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    FreeCol.main(new String[]{}, MainActivity.this);
                }
            }).start();

            // Setup stub classes
            Log.d("dataHome", "Setup stub classes");
            //Toast.makeText(getApplicationContext(), "Setup stub classes", Toast.LENGTH_SHORT).show();
            SwingUtilities.setActivity(this);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            GraphicsConfiguration.setBounds(width, height);

            // Setup directories
            Log.d("dataHome", "Setup directories");
            //Toast.makeText(getApplicationContext(), "Setup directories", Toast.LENGTH_SHORT).show();
            FreeCol.setSaveDirectory(new File(dataHome));
        }
        catch (Exception e) {
            Log.e("error", e.getMessage());
            //Toast.makeText(getApplicationContext(),"error:"+e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
			Fragment currentFrag = getFragmentManager().findFragmentById(
					R.id.content);
			if (currentFrag != null && currentFrag instanceof GameFragment) {
				GameMenuFragment menuFrag = new GameMenuFragment();
				menuFrag.setClient(FreeCol.getFreeColClient());
				menuFrag.show(getFragmentManager(), "");
				return true;
			}
    	}
    	return super.onKeyDown(keyCode, event);
    }

    private  void UnzipFromAssets(String filename, String destDir)
    {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        try {
            in = assetManager.open(filename);
            ZipInputStream zis = new ZipInputStream(in);
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(destDir, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } finally {
                        fout.close();
                    }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
                }
            } finally {
                zis.close();
            }

            in.close();
            in = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private void copyFile(String filename, String destDir) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = destDir + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize);
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return formatSize(totalBlocks * blockSize);
    }

    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return formatSize(availableBlocks * blockSize);
        } else {
            return ERROR;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return formatSize(totalBlocks * blockSize);
        } else {
            return ERROR;
        }
    }

    public static long formatSize(long size) {
        if (size >= 1024) {
            size /= 1024;
            return size;
        }
        else
        {
            return 1;
        }
//        String suffix = null;
//
//        if (size >= 1024) {
//            suffix = "KB";
//            size /= 1024;
//            if (size >= 1024) {
//                suffix = "MB";
//                size /= 1024;
//            }
//        }

//        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
//
//        int commaOffset = resultBuffer.length() - 3;
//        while (commaOffset > 0) {
//            resultBuffer.insert(commaOffset, ',');
//            commaOffset -= 3;
//        }
//
//        if (suffix != null) resultBuffer.append(suffix);
//        return resultBuffer.toString();
    }
}