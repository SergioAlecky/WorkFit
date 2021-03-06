/**
 * Filename  : MailChecker.java
 *
 * ***************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************************
 * Project    : WorkFit
 *
 * Author     : Sergio Alecky
 *
 * last change by : $Author:$ 
 * last check in  : $Date: $
 */

package by.uniterra.system.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import by.uniterra.system.iface.IGlobalProperties;
import by.uniterra.system.model.SystemModel;
import by.uniterra.system.util.DateUtils;
import by.uniterra.udi.util.Log;
import by.uniterra.udi.util.LogParser;

/**
 * The <code>MailChecker</code> is used for read new mail
 *
 *
 * @author Sergio Alecky
 * @since 28 окт. 2014 г.
 */
public class MailChecker
{

    /**
     * @param args
     *
     * @author Sergio Alecky
     * @date 28 окт. 2014 г.
     */
    private static String SERVER = SystemModel.getString(IGlobalProperties.ML_SERVER, "");
    private static String EMAIL = SystemModel.getString(IGlobalProperties.ML_EMAIL, "");
    private static String PASSWORD = SystemModel.getString(IGlobalProperties.ML_PWD, "");

    public static void main(String[] args)
    {
        SystemModel.initJPA();
        checkMail();

    }

    public static void checkMail()
    {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.put("mail.imap.port", "143");
        // byte[] strToSave = null;

        try
        {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(SERVER, EMAIL, PASSWORD);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            // search for all "unseen" messages
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

            // Message message[] = folder.getMessages();

            Message message[] = inbox.search(unseenFlagTerm);
            String out = message.length == 0 ? ("No new  messages found...") : ("We have: " + message.length + " new mail");
            System.out.println(out);

            for (int i = 0, n = message.length; i < n; i++)
            {
                System.out.println(i + 1 + ": " + message[i].getContentType());

                Object objMp = message[i].getContent();
                // String contentType = message[i].getContentType();

                if (objMp instanceof Multipart || objMp instanceof MimeMultipart)
                {
                    findLogInMultipart((Multipart)objMp);
                }
                else if (objMp instanceof String)
                {
                    System.out.println("CONTENT:" + objMp);
                    createFileFromMail(((String) objMp).getBytes());
                }
                System.out.println("SENT DATE:" + DateUtils.toGMT(message[i].getSentDate()));
                System.out.println("SUBJECT:" + message[i].getSubject());

                // mark as read
                // message[i].setFlags(seen, true);
            }
            // close
            inbox.close(false);
            store.close();
            System.exit(0);

        }
        catch (Exception mex)
        {
            mex.printStackTrace();
        }
    }

    public static boolean createFileFromMail(byte[] strToSave) 
    {
        boolean bResult = false;
        Path path = Paths.get("D:/temp.txt");

        try
        {
            Files.write(path, strToSave, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            Log.error(MailChecker.class, e, "createFileFromMail error ");
        }

        String logDate = LogParser.isALog(path);

        if (logDate.length() != 0)
        {
            System.out.println("Лог за: " + logDate);
            try
            {
                Files.move(path, path.resolveSibling(logDate.replace(":", "-") + ".txt"));
                bResult = true;
            }
            catch (IOException e)
            {
                Log.error(MailChecker.class, e, "createFileFromMail error ");
            }
        }
        else
        {
            try
            {
                Files.delete(path);
            }
            catch (IOException e)
            {
                Log.error(MailChecker.class, e, "createFileFromMail error ");
            }
        }
        return bResult;
    }


    public static boolean findLogInMultipart(Multipart multiPart) throws MessagingException, IOException
    {
        int numberOfParts = multiPart.getCount();
        for (int partCount = 0; partCount < numberOfParts; partCount++)
        {
            BodyPart part = (BodyPart) multiPart.getBodyPart(partCount);
            Object objBodyPartContent = part.getContent();
            if (objBodyPartContent instanceof MimeMultipart)
            {
                if(findLogInMultipart((Multipart) objBodyPartContent))
                {
                    return true;
                }
            }
            else
            {
                if (objBodyPartContent instanceof byte[])
                {
                    if (createFileFromMail((byte[]) objBodyPartContent))
                    {
                        return true;
                    }
                }
                if (objBodyPartContent instanceof String)
                {
                    if (createFileFromMail(((String) objBodyPartContent).getBytes()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static byte[] getBytesFromInputStream(InputStream is)
    {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();)
        {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1;)
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
