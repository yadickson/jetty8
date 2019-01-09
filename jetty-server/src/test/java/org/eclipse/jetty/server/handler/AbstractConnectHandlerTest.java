//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server.handler;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractConnectHandlerTest
{
    protected Server server;
    protected Connector serverConnector;
    protected Server proxy;
    protected Connector proxyConnector;

    protected void startServer(Connector connector, Handler handler) throws Exception
    {
        server = new Server();
        serverConnector = connector;
        server.addConnector(serverConnector);
        server.setHandler(handler);
        server.start();
    }

    protected void startProxy() throws Exception
    {
        startProxy(new SelectChannelConnector(), new ConnectHandler());
    }

    protected void startProxy(Connector connector, ConnectHandler connectHandler) throws Exception
    {
        proxy = new Server();
        proxyConnector = connector;
        proxy.addConnector(proxyConnector);
        proxy.setHandler(connectHandler);
        proxy.start();
    }

    @After
    public void stop() throws Exception
    {
        stopProxy();
        stopServer();
    }

    protected void stopServer() throws Exception
    {
        server.stop();
        server.join();
    }

    protected void stopProxy() throws Exception
    {
        proxy.stop();
        proxy.join();
    }

    protected Response readResponse(BufferedReader reader) throws IOException
    {
        // Simplified parser for HTTP responses
        String line = reader.readLine();
        if (line == null)
            throw new EOFException();
        Matcher responseLine = Pattern.compile("HTTP/1\\.1\\s+(\\d+)").matcher(line);
        assertTrue(responseLine.lookingAt());
        String code = responseLine.group(1);

        Map<String, String> headers = new LinkedHashMap<String, String>();
        while ((line = reader.readLine()) != null)
        {
            if (line.trim().length() == 0)
                break;

            Matcher header = Pattern.compile("([^:]+):\\s*(.*)").matcher(line);
            assertTrue(header.lookingAt());
            String headerName = header.group(1);
            String headerValue = header.group(2);
            headers.put(headerName.toLowerCase(Locale.ENGLISH), headerValue.toLowerCase(Locale.ENGLISH));
        }

        StringBuilder body;
        if (headers.containsKey("content-length"))
        {
            int readLen = 0;
            int length = Integer.parseInt(headers.get("content-length"));
            body = new StringBuilder(length);
            try
            {
                for (int i = 0; i < length; ++i)
                {
                    char c = (char)reader.read();
                    body.append(c);
                    readLen++;
                }
            }
            catch (SocketTimeoutException e)
            {
                System.err.printf("Read %,d bytes (out of an expected %,d bytes)%n", readLen, length);
                throw e;
            }
        }
        else if ("chunked".equals(headers.get("transfer-encoding")))
        {
            body = new StringBuilder(64 * 1024);
            while ((line = reader.readLine()) != null)
            {
                if ("0".equals(line))
                {
                    line = reader.readLine();
                    assertEquals("", line);
                    break;
                }

                try
                {
                    Thread.sleep(5);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                int length = Integer.parseInt(line, 16);
                for (int i = 0; i < length; ++i)
                {
                    char c = (char)reader.read();
                    body.append(c);
                }
                line = reader.readLine();
                assertEquals("", line);
            }
        }
        else throw new IllegalStateException();

        return new Response(code, headers, body.toString().trim());
    }

    protected Socket newSocket() throws IOException
    {
        Socket socket = new Socket("localhost", proxyConnector.getLocalPort());
        socket.setSoTimeout(10000);
        return socket;
    }

    protected class Response
    {
        private final String code;
        private final Map<String, String> headers;
        private final String body;

        private Response(String code, Map<String, String> headers, String body)
        {
            this.code = code;
            this.headers = headers;
            this.body = body;
        }

        public String getCode()
        {
            return code;
        }

        public Map<String, String> getHeaders()
        {
            return headers;
        }

        public String getBody()
        {
            return body;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(code).append("\r\n");
            for (Map.Entry<String, String> entry : headers.entrySet())
                builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            builder.append("\r\n");
            builder.append(body);
            return builder.toString();
        }
    }
}
