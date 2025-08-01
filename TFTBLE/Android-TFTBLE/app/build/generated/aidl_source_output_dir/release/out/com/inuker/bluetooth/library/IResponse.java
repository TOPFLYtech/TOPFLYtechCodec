/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.inuker.bluetooth.library;
// Declare any non-default types here with import statements

public interface IResponse extends android.os.IInterface
{
  /** Default implementation for IResponse. */
  public static class Default implements com.inuker.bluetooth.library.IResponse
  {
    @Override public void onResponse(int code, android.os.Bundle data) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.inuker.bluetooth.library.IResponse
  {
    private static final java.lang.String DESCRIPTOR = "com.inuker.bluetooth.library.IResponse";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.inuker.bluetooth.library.IResponse interface,
     * generating a proxy if needed.
     */
    public static com.inuker.bluetooth.library.IResponse asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.inuker.bluetooth.library.IResponse))) {
        return ((com.inuker.bluetooth.library.IResponse)iin);
      }
      return new com.inuker.bluetooth.library.IResponse.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_onResponse:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          android.os.Bundle _arg1;
          if ((0!=data.readInt())) {
            _arg1 = android.os.Bundle.CREATOR.createFromParcel(data);
          }
          else {
            _arg1 = null;
          }
          this.onResponse(_arg0, _arg1);
          reply.writeNoException();
          if ((_arg1!=null)) {
            reply.writeInt(1);
            _arg1.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements com.inuker.bluetooth.library.IResponse
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void onResponse(int code, android.os.Bundle data) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(code);
          if ((data!=null)) {
            _data.writeInt(1);
            data.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onResponse, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onResponse(code, data);
            return;
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            data.readFromParcel(_reply);
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static com.inuker.bluetooth.library.IResponse sDefaultImpl;
    }
    static final int TRANSACTION_onResponse = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(com.inuker.bluetooth.library.IResponse impl) {
      // Only one user of this interface can use this function
      // at a time. This is a heuristic to detect if two different
      // users in the same process use this function.
      if (Stub.Proxy.sDefaultImpl != null) {
        throw new IllegalStateException("setDefaultImpl() called twice");
      }
      if (impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static com.inuker.bluetooth.library.IResponse getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void onResponse(int code, android.os.Bundle data) throws android.os.RemoteException;
}
