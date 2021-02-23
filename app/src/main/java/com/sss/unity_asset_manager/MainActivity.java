package com.sss.unity_asset_manager;

import android.content.Context;
import android.os.AsyncTask;

import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.IOException;

public class MainActivity {
    /**
     * Устанавливает имя Unity GameObject методы которого будут вызываться в библотеке
     * @param name this.gameObject.name
     */
    public static void setReceiverObjectName( String name ) { _receiverObjectName = name; }
    /**
     * Устанавливает имя метода\callback'a обрабатывающего лог-сообщения библиотеки
     * @param name имя метода\callback'a
     */
    public static void setLogReceiverMethodName( String name ) { _logReceiverMethodName = name; }
    /**
     * Устанавливает имя метода\callback'a обрабатывающего результат копирования ассетов
     * @param name имя метода\callback'a
     */
    public static void setOnCopyingCompleteMethod( String name ) { _onCopyingCompleteMethodName = name; }
    /**
     * В ассинхронном режиме копирует файлы из StreamingAssets
     * в external storage
     * @param srcDirPath путь к директории содержащей файл assets.lst
     * в котором указан набор файлов для копирования в external storage.
     * Файл assets.lst следует располагать в StreamingAssets
     * @throws IOException
     */
    public void tryCopyStreamingAssets2ExternalStorage( String srcDirPath ) throws IOException {
        final String SRC_DIR_PATH = srcDirPath;

        new AsyncTask<Void, Void, File>( ) {
            @Override
            protected File doInBackground( Void... params ) {
                File assetDir = null;
                try {
                    UnityAssetsObb.logReceiverMethodName = _logReceiverMethodName;
                    UnityAssetsObb.receiverObjectName = _receiverObjectName;
                    UnityAssetsObb assetsObb = new UnityAssetsObb( _context, SRC_DIR_PATH );
                    if ( assetsObb.exist( ) ) {
                        assetDir = assetsObb.syncAssets();
                    }
                    else {
                        UnityAssets.logReceiverMethodName = _logReceiverMethodName;
                        UnityAssets.receiverObjectName = _receiverObjectName;
                        UnityAssets assetsApk = new UnityAssets( _context, SRC_DIR_PATH );
                        assetDir = assetsApk.syncAssets( );
                    }
                } catch ( IOException e ) {
                    copyingResult( EMPTY_DIR_PATH );
                    return null;
                }
                return assetDir;
            }
            @Override
            protected void onPostExecute( File assets ) {
                if ( assets != null ) {
                    copyingResult( assets.getAbsolutePath( ) );
                } else {
                    copyingResult( EMPTY_DIR_PATH );
                    toUnityLog( "Failed to copy files from streaming assets to external storage" );
                }
            }
        }.execute( );
    }

    /**
     * Не использовать данный конструктор!
     * @throws IOException
     */
    public MainActivity( ) throws IOException {

    }

    /**
     * Конктсруктор UnityStreamingAssetsManager
     * @param context javaUnityPlayer.GetStatic<AndroidJavaObject>( "currentActivity" );
     * @throws IOException
     */
    public MainActivity( Context context ) throws IOException {
        _context = context;
    }

    private static void toUnityLog( String message ) {
        if ( ( _receiverObjectName != null ) && ( _logReceiverMethodName != null ) )
            UnityPlayer.UnitySendMessage( _receiverObjectName, _logReceiverMethodName, message );
    }

    private static void copyingResult( String message ) {
        if ( ( _receiverObjectName != null ) && ( _onCopyingCompleteMethodName != null ) )
            UnityPlayer.UnitySendMessage( _receiverObjectName, _onCopyingCompleteMethodName, message );
    }

    private static String _receiverObjectName = null;
    private static String _logReceiverMethodName = null;
    private static String _onCopyingCompleteMethodName = null;

    private Context _context;
    private final String EMPTY_DIR_PATH = "";
}
