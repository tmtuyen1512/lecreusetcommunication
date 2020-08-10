package com.delfi.xmobile.app.lecreusetcommunication.ftpcom;

import org.apache.commons.net.ftp.FTPSClient;
import javax.net.ssl.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;

public class SSLSessionReuseFTPSClient extends FTPSClient {


    public SSLSessionReuseFTPSClient() {
        super();
    }

    public SSLSessionReuseFTPSClient(boolean isImplicit) {
        super(isImplicit);
    }

    @Override
    public void connect(String hostname, int port) throws IOException {
        super.connect(hostname, port);
        if(_socket_ instanceof SSLSocket)
        ((SSLSocket) _socket_).addHandshakeCompletedListener(new HandshakeCompletedListener() {
            @Override
            public void handshakeCompleted(HandshakeCompletedEvent event) {
                System.out.println(event.toString());
            }
        });
    }

    @Override
    protected void _prepareDataSocket_(Socket socket) throws IOException {
        if (socket instanceof SSLSocket) {
            final SSLSession session = ((SSLSocket) _socket_).getSession();
            final SSLSessionContext context = session.getSessionContext();
            try {
                Field sessionHostPortCache = context.getClass().getDeclaredField("sessionsByHostAndPort");
                sessionHostPortCache.setAccessible(true);
                Object cache = sessionHostPortCache.get(context); //Map

                Method getEntrySetMethod = cache.getClass().getDeclaredMethod("entrySet");
                getEntrySetMethod.setAccessible(true);
                Object entrySet = getEntrySetMethod.invoke(cache);

                Method getIteratorMethod = entrySet.getClass().getDeclaredMethod("iterator");
                getIteratorMethod.setAccessible(true);
                Object iterator = getIteratorMethod.invoke(entrySet);

                Method getNextMethod = iterator.getClass().getDeclaredMethod("next");
                getNextMethod.setAccessible(true);
                Object element = getNextMethod.invoke(iterator);

                Method getKeyMethod = element.getClass().getDeclaredMethod("getKey");
                getKeyMethod.setAccessible(true);
                Object key = getKeyMethod.invoke(element);

                Method getValMethod = element.getClass().getDeclaredMethod("getValue");
                getValMethod.setAccessible(true);
                Object val = getValMethod.invoke(element);

                Constructor<?> constructor = key.getClass().getDeclaredConstructor(String.class, int.class);
                constructor.setAccessible(true);


                Object newKey = constructor.newInstance(null, 0);
                Field[] allFields = key.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    field.setAccessible(true);
                    field.set(newKey, field.get(key));
                }

                Field port = newKey.getClass().getDeclaredField("port");
                port.setAccessible(true);
                port.set(newKey, socket.getPort());

                Method putMethod = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
                putMethod.setAccessible(true);
                putMethod.invoke(cache, newKey, val);

                /*final Field sessionHostPortCache = context.getClass().getDeclaredField("sessionHostPortCache");
                sessionHostPortCache.setAccessible(true);
                final Object cache = sessionHostPortCache.get(context);
                final Method putMethod = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
                putMethod.setAccessible(true);
                final Method getHostMethod = socket.getClass().getDeclaredMethod("getHost");
                getHostMethod.setAccessible(true);
                Object host = getHostMethod.invoke(socket);
                final String key = String.format("%s:%s", host, String.valueOf(socket.getPort())).toLowerCase(Locale.ROOT);
                putMethod.invoke(cache, key, session);*/

                System.out.println(socket.getPort());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}
