/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.inuker.bluetooth.library;
public interface IBluetoothService extends android.os.IInterface
{
  /** Default implementation for IBluetoothService. */
  public static class Default implements com.inuker.bluetooth.library.IBluetoothService
  {
    @Override public void callBluetoothApi(int code, android.os.Bundle args, com.inuker.bluetooth.library.IResponse response) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.inuker.bluetooth.library.IBluetoothService
  {
    private static final java.lang.String DESCRIPTOR = "com.inuker.bluetooth.library.IBluetoothService";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.inuker.bluetooth.library.IBluetoothService interface,
     * generating a proxy if needed.
     */
    public static com.inuker.bluetooth.library.IBluetoothService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.inuker.bluetooth.library.IBluetoothService))) {
        return ((com.inuker.bluetooth.library.IBluetoothService)iin);
      }
      return new com.inuker.bluetooth.library.IBluetoothService.Stub.Proxy(obj);
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
        case TRANSACTION_callBluetoothApi:
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
          com.inuker.bluetooth.library.IResponse _arg2;
          _arg2 = com.inuker.bluetooth.library.IResponse.Stub.asInterface(data.readStrongBinder());
          this.callBluetoothApi(_arg0, _arg1, _arg2);
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
    private static class Proxy implements com.inuker.bluetooth.library.IBluetoothService
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
      @Override public void callBluetoothApi(int code, android.os.Bundle args, com.inuker.bluetooth.library.IResponse response) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(code);
          if ((args!=null)) {
            _data.writeInt(1);
            args.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeStrongBinder((((response!=null))?(response.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_callBluetoothApi, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().callBluetoothApi(code, args, response);
            return;
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            args.readFromParcel(_reply);
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static com.inuker.bluetooth.library.IBluetoothService sDefaultImpl;
    }
    static final int TRANSACTION_callBluetoothApi = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(com.inuker.bluetooth.library.IBluetoothService impl) {
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
    public static com.inuker.bluetooth.library.IBluetoothService getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void callBluetoothApi(int code, android.os.Bundle args, com.inuker.bluetooth.library.IResponse response) throws android.os.RemoteException;
}
