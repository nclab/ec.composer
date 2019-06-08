/*
 * Copyright 2018 Jonathan Chang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.metacontext.ec.prototype.composer;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Settings {

    public static String getTimeBasedFilename() {
        return LocalDateTime.now().toString().replace(":", "-").substring(0, 25);
    }

    public static String LOG_PATH = "log/";

    public static String LOG_PATH_TEST = "log/test/";

    public static String SER_PATH = "ser/";
    
    public static enum LogState {
        DEFAULT, TEST, DISABLED
    }

    public static void setFileHandler(LogState STATE, Logger logger)
            throws Exception {

        logger.log(Level.INFO,
                "Setting FileHandler, STATE = {0}", STATE);
        logger.setUseParentHandlers(false);

        File file_path;
        switch (STATE) {
            case DISABLED:
                logger.setUseParentHandlers(false);
                return;
            case TEST:
                file_path = new File(LOG_PATH_TEST);
                break;
            case DEFAULT:
            default:
                file_path = new File(LOG_PATH);
        }
        if (!file_path.exists() || !file_path.isDirectory()) {
            file_path.mkdirs();
        }
        FileHandler fh = new FileHandler(Path.of(file_path.getPath(),
                getTimeBasedFilename() + ".log").toString(), true);
        fh.setEncoding("UTF-8");
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);
    }
}
