package com.flyingpigeon.library;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.flyingpigeon.library.Config.PREFIX;

/**
 * @author ringle-android
 * @date 20-6-11
 * @since 1.0.0
 */
public final class PigeonEngine {

    static final String PREXFIX_ROUTE = "route-";
    static final String PREXFIX_METHOD = "method-";
    static final String KEY_LOOK_UP_APPROACH = "key_look_up_approach";
    static final int APPROACH_METHOD = 1;
    static final int APPROACH_ROUTE = 2;
    static final String KEY_RESPONSE_CODE = "reponse_code";
    static final int RESPONSE_RESULE_NO_SUCH_METHOD = 1;
    static final String KEY_LENGTH = "key_length";
    static final String KEY_INDEX = "key_%s";


    private static final String TAG = PREFIX + PigeonEngine.class.getSimpleName();
    private Gson mGson = new Gson();
    private ConcurrentHashMap<String, MethodCaller> callers = new ConcurrentHashMap<>();

    private PigeonEngine() {
    }

    private static final PigeonEngine sInstance = new PigeonEngine();

    public static PigeonEngine getInstance() {
        return sInstance;
    }


    Bundle buildRequest(Method method, Object[] args) {
        Bundle bundle = new Bundle();
        Type[] types = method.getGenericParameterTypes();
        String key = KEY_INDEX;
        for (int i = 0; i < types.length; i++) {
            Log.e(TAG, "type name:" + types[i] + " method:" + method.getName() + " Parcelable.class.isAssignableFrom(((Class<?>) types[i])):" + Parcelable.class.isAssignableFrom(((Class<?>) types[i])) + " key:" + String.format(key, i + ""));
            if (int.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.IntHandler handler = (ParameterHandler.IntHandler) map.get(int.class);
                assert handler != null;
                handler.apply((Integer) args[i], String.format(key, i + ""), bundle);
            } else if (double.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.DoubleHandler handler = (ParameterHandler.DoubleHandler) map.get(double.class);
                assert handler != null;
                handler.apply((Double) args[i], String.format(key, i + ""), bundle);
            } else if (long.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.LongHandler handler = (ParameterHandler.LongHandler) map.get(long.class);
                assert handler != null;
                handler.apply((Long) args[i], String.format(key, i + ""), bundle);
            } else if (short.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.ShortHandler handler = (ParameterHandler.ShortHandler) map.get(short.class);
                assert handler != null;
                handler.apply((Short) args[i], String.format(key, i + ""), bundle);
            } else if (float.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.FloatHandler handler = (ParameterHandler.FloatHandler) map.get(float.class);
                assert handler != null;
                handler.apply((Float) args[i], String.format(key, i + ""), bundle);
            } else if (byte.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.ByteHandler handler = (ParameterHandler.ByteHandler) map.get(byte.class);
                assert handler != null;
                handler.apply((Byte) args[i], String.format(key, i + ""), bundle);
            } else if (boolean.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.BooleanHandler handler = (ParameterHandler.BooleanHandler) map.get(boolean.class);
                assert handler != null;
                handler.apply((Boolean) args[i], String.format(key, i + ""), bundle);
            } else if (String.class.isAssignableFrom((Class<?>) types[i])) {
                ParameterHandler.StringHandler handler = (ParameterHandler.StringHandler) map.get(String.class);
                assert handler != null;
                handler.apply((String) args[i], String.format(key, i + ""), bundle);
            } else if (Parcelable.class.isAssignableFrom(((Class<?>) types[i]))) {
                ParameterHandler.ParcelableHandler handler = (ParameterHandler.ParcelableHandler) map.get(Parcelable.class);
                assert handler != null;
                handler.apply((Parcelable) args[i], String.format(key, i + ""), bundle);
                Parcelable parcelable = bundle.getParcelable(String.format(key, i + ""));
                Log.e(TAG, "parcelable:" + mGson.toJson(parcelable));
            } else if (Serializable.class.isAssignableFrom(((Class<?>) types[i]))) {
                ParameterHandler.SerializableHandler handler = (ParameterHandler.SerializableHandler) map.get(Serializable.class);
                assert handler != null;
                handler.apply((Serializable) args[i], String.format(key, i + ""), bundle);
                Parcelable parcelable = bundle.getParcelable(String.format(key, i + ""));
                Log.e(TAG, "parcelable:" + mGson.toJson(parcelable));
            }

        }
        bundle.putInt(KEY_LENGTH, types.length);
        bundle.putInt(KEY_LOOK_UP_APPROACH, APPROACH_METHOD);
        return bundle;
    }

    MethodCaller parseRequest(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) throws NoSuchMethodException, IllegalAccessException {
        Log.e(TAG, "call:" + method + " arg:" + mGson.toJson(arg) + " size:" + extras.size() + " ServiceContentProvider.serviceContext:" + ServiceContentProvider.serviceContext);
        MethodCaller methodCaller;
        int approach = extras.getInt(KEY_LOOK_UP_APPROACH);
        Object owner = null;
        if (approach == APPROACH_METHOD) {
            owner = ServiceContentProvider.serviceContext;
        }
        if (approach == APPROACH_METHOD && (methodCaller = lookupMethodByCache(method)) != null) {
            return methodCaller;
        }
        String key = KEY_INDEX;
        int length = extras.getInt(KEY_LENGTH);
        Class<?>[] clazzs = new Class[length];
        Log.e(TAG, "length:" + length);
        for (int i = 0; i < length; i++) {
            Parcelable parcelable = extras.getParcelable(String.format(key, i + ""));
            if (parcelable == null) {
                Log.e(TAG, "break index:" + i + " key:" + String.format(key, i + ""));
                break;
            }
            android.util.Pair<Class<?>, Object> data = parcelableToClazz(parcelable);
            clazzs[i] = data.first;
            Log.e(TAG, "parcelable:" + parcelable.toString() + " data.first:" + data.first + " key:" + String.format(key, i + ""));
        }
        for (int i = 0; i < length; i++) {
            if (clazzs[i] == null) {
                throw new IllegalArgumentException("arg error");
            }
        }
        assert owner != null;
        Log.e(TAG, "method:" + method + "  clazzs:" + clazzs.length + "  clazz:" + clazzs);
        Method target = owner.getClass().getDeclaredMethod(method, clazzs);
        target.setAccessible(true);
        methodCaller = new Caller(target, "", ServiceContentProvider.serviceContext);
        cacheMethodToMemory(methodCaller);
        return methodCaller;
    }


    Object[] parseData(@Nullable String arg, @Nullable Bundle extras) {
        String key = KEY_INDEX;
        int length = extras.getInt(KEY_LENGTH);
        Object[] values = new Object[length];
        Class<?>[] clazzs = new Class[length];
        for (int i = 0; i < length; i++) {
            Parcelable parcelable = extras.getParcelable(String.format(key, i + ""));
            if (parcelable == null) {
                break;
            }
            Log.e(TAG, "parcelable:" + mGson.toJson(parcelable) + " parcelable:" + parcelable);
            android.util.Pair<Class<?>, Object> data = parcelableToClazz(parcelable);
            clazzs[i] = data.first;
            values[i] = data.second;
        }
        return values;
    }

    private void cacheMethodToMemory(MethodCaller methodCaller) {
        callers.put(methodCaller.callerId(), methodCaller);
    }

    private MethodCaller lookupMethodByCache(String method) {
        return callers.get(PREXFIX_METHOD + method);
    }

    private static final ConcurrentHashMap<Class, ParameterHandler> map = new ConcurrentHashMap<Class, ParameterHandler>() {
        {
            put(int.class, new ParameterHandler.IntHandler());
            put(double.class, new ParameterHandler.DoubleHandler());
            put(long.class, new ParameterHandler.LongHandler());
            put(short.class, new ParameterHandler.ShortHandler());
            put(float.class, new ParameterHandler.FloatHandler());
            put(byte.class, new ParameterHandler.ByteHandler());
            put(boolean.class, new ParameterHandler.BooleanHandler());
            put(Parcelable.class, new ParameterHandler.ParcelableHandler());
            put(Serializable.class, new ParameterHandler.SerializableHandler());
            put(String.class, new ParameterHandler.StringHandler());
        }
    };

    private android.util.Pair<Class<?>, Object> parcelableToClazz(Parcelable parcelable) {
        if (parcelable instanceof com.flyingpigeon.library.Pair.PairInt) {
            return new android.util.Pair<Class<?>, Object>(int.class, ((com.flyingpigeon.library.Pair.PairInt) parcelable).getValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairDouble) {
            return new android.util.Pair<Class<?>, Object>(double.class, ((com.flyingpigeon.library.Pair.PairDouble) parcelable).getValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairLong) {
            return new android.util.Pair<Class<?>, Object>(long.class, ((com.flyingpigeon.library.Pair.PairLong) parcelable).getValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairShort) {
            return new android.util.Pair<Class<?>, Object>(short.class, ((com.flyingpigeon.library.Pair.PairShort) parcelable).getValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairFloat) {
            return new android.util.Pair<Class<?>, Object>(float.class, ((com.flyingpigeon.library.Pair.PairFloat) parcelable).getValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairByte) {
            return new android.util.Pair<Class<?>, Object>(byte.class, ((com.flyingpigeon.library.Pair.PairByte) parcelable).getValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairBoolean) {
            return new android.util.Pair<Class<?>, Object>(boolean.class, ((com.flyingpigeon.library.Pair.PairBoolean) parcelable).isValue());
        } else if (parcelable instanceof com.flyingpigeon.library.Pair.PairString) {
            try {
                return new android.util.Pair<Class<?>, Object>(Class.forName(((Pair.PairString) parcelable).getKey()), ((Pair.PairString) parcelable).getValue());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }  else if (parcelable instanceof com.flyingpigeon.library.Pair.PairSerializable) {
            try {
                return new android.util.Pair<Class<?>, Object>(Class.forName(((Pair.PairSerializable) parcelable).getKey()), ((Pair.PairSerializable) parcelable).getValue());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }else {
            try {
                return new android.util.Pair<Class<?>, Object>(Class.forName(((Pair.PairParcelable) parcelable).getKey()), ((Pair.PairParcelable) parcelable).getValue());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}