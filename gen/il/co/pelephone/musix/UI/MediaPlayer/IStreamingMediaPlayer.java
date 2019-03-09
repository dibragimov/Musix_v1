/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\android-sdk-windows_old\\prj\\Musix\\src\\il\\co\\pelephone\\musix\\UI\\MediaPlayer\\IStreamingMediaPlayer.aidl
 */
package il.co.pelephone.musix.UI.MediaPlayer;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
// Interface for Streaming Player.

public interface IStreamingMediaPlayer extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer
{
private static final java.lang.String DESCRIPTOR = "il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IStreamingMediaPlayer interface,
 * generating a proxy if needed.
 */
public static il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer))) {
return ((il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer)iin);
}
return new il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_isPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_startAudio:
{
data.enforceInterface(DESCRIPTOR);
this.startAudio();
reply.writeNoException();
return true;
}
case TRANSACTION_pauseAudio:
{
data.enforceInterface(DESCRIPTOR);
this.pauseAudio();
reply.writeNoException();
return true;
}
case TRANSACTION_getSongID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getSongID();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPlayedTime:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPlayedTime();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_playNext:
{
data.enforceInterface(DESCRIPTOR);
this.playNext();
reply.writeNoException();
return true;
}
case TRANSACTION_playPrevious:
{
data.enforceInterface(DESCRIPTOR);
this.playPrevious();
reply.writeNoException();
return true;
}
case TRANSACTION_setRepeatPlayback:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.setRepeatPlayback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setShufflePlaylist:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.setShufflePlaylist(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// Check to see if service is playing audio

public boolean isPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Start playing audio

public void startAudio() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startAudio, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//pause playing audio

public void pauseAudio() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pauseAudio, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//6. The UI layer needs to be able to query for played time, and song ID (the other stuff it can take from the data layer) (Ron Srebro, Skype chat)

public java.lang.String getSongID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSongID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//6. The UI layer needs to be able to query for played time, and song ID 

public int getPlayedTime() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPlayedTime, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// stops the service

public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//Start next audio

public void playNext() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_playNext, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//Start previous audio

public void playPrevious() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_playPrevious, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//repeat playing music after the end is reached

public void setRepeatPlayback(boolean repeating) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((repeating)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setRepeatPlayback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//shuffle the playlist

public void setShufflePlaylist(boolean shuffling) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((shuffling)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setShufflePlaylist, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_isPlaying = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startAudio = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_pauseAudio = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getSongID = (IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getPlayedTime = (IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_stop = (IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_playNext = (IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_playPrevious = (IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_setRepeatPlayback = (IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_setShufflePlaylist = (IBinder.FIRST_CALL_TRANSACTION + 9);
}
// Check to see if service is playing audio

public boolean isPlaying() throws android.os.RemoteException;
//Start playing audio

public void startAudio() throws android.os.RemoteException;
//pause playing audio

public void pauseAudio() throws android.os.RemoteException;
//6. The UI layer needs to be able to query for played time, and song ID (the other stuff it can take from the data layer) (Ron Srebro, Skype chat)

public java.lang.String getSongID() throws android.os.RemoteException;
//6. The UI layer needs to be able to query for played time, and song ID 

public int getPlayedTime() throws android.os.RemoteException;
// stops the service

public void stop() throws android.os.RemoteException;
//Start next audio

public void playNext() throws android.os.RemoteException;
//Start previous audio

public void playPrevious() throws android.os.RemoteException;
//repeat playing music after the end is reached

public void setRepeatPlayback(boolean repeating) throws android.os.RemoteException;
//shuffle the playlist

public void setShufflePlaylist(boolean shuffling) throws android.os.RemoteException;
}
