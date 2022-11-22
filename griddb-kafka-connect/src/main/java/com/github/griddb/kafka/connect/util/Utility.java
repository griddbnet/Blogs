/*
 * Copyright (c) 2021 TOSHIBA Digital Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.griddb.kafka.connect.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Utility {

    /**
     * Get the config value in connector.properties
     */
    public static String getConfigString(String name, String defaultValue) {
        Properties properties = new Properties();
        InputStream iStream = null;
        String configString = defaultValue;
        try {
            // Loading properties file from the classpath
            iStream = Utility.class.getClass().getResourceAsStream("connector.properties");
            if (iStream == null) {
                return defaultValue;
            }
            properties.load(iStream);
            configString = properties.getProperty(name, defaultValue);
            iStream.close();
        } catch (IOException e) {
            return defaultValue;
        }
        return configString;
    }
}
