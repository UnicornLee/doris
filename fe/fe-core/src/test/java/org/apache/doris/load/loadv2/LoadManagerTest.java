// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.load.loadv2;

import org.apache.doris.catalog.Database;
import org.apache.doris.catalog.Env;
import org.apache.doris.catalog.Table;
import org.apache.doris.common.Config;
import org.apache.doris.common.FeMetaVersion;
import org.apache.doris.common.jmockit.Deencapsulation;
import org.apache.doris.datasource.InternalCatalog;
import org.apache.doris.meta.MetaContext;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

public class LoadManagerTest {
    private LoadManager loadManager;
    private final String fieldName = "idToLoadJob";

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        File file = new File("./loadManagerTest");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testSerializationNormal(@Mocked Env env, @Mocked InternalCatalog catalog, @Injectable Database database,
            @Injectable Table table) throws Exception {
        new Expectations() {
            {
                env.getInternalCatalog();
                minTimes = 0;
                result = catalog;
                catalog.getDbNullable(anyLong);
                minTimes = 0;
                result = database;
                database.getTableNullable(anyLong);
                minTimes = 0;
                result = table;
                table.getName();
                minTimes = 0;
                result = "tablename";
                Env.getCurrentEnvJournalVersion();
                minTimes = 0;
                result = FeMetaVersion.VERSION_CURRENT;
            }
        };

        loadManager = new LoadManager(new LoadJobScheduler());
        LoadJob job1 = new InsertLoadJob("job1", 1L, 1L, 1L, System.currentTimeMillis(), "", "");
        Deencapsulation.invoke(loadManager, "addLoadJob", job1);

        File file = serializeToFile(loadManager);

        LoadManager newLoadManager = deserializeFromFile(file);

        Map<Long, LoadJob> loadJobs = Deencapsulation.getField(loadManager, fieldName);
        Map<Long, LoadJob> newLoadJobs = Deencapsulation.getField(newLoadManager, fieldName);
        Assert.assertEquals(loadJobs, newLoadJobs);
    }

    @Test
    public void testSerializationWithJobRemoved(@Mocked MetaContext metaContext, @Mocked Env env,
            @Mocked InternalCatalog catalog, @Injectable Database database, @Injectable Table table) throws Exception {
        new Expectations() {
            {
                env.getInternalCatalog();
                minTimes = 0;
                result = catalog;
                catalog.getDbNullable(anyLong);
                minTimes = 0;
                result = database;
                database.getTableNullable(anyLong);
                minTimes = 0;
                result = table;
                table.getName();
                minTimes = 0;
                result = "tablename";
                Env.getCurrentEnvJournalVersion();
                minTimes = 0;
                result = FeMetaVersion.VERSION_CURRENT;
            }
        };

        loadManager = new LoadManager(new LoadJobScheduler());
        LoadJob job1 = new InsertLoadJob("job1", 1L, 1L, 1L, System.currentTimeMillis(), "", "");
        Deencapsulation.invoke(loadManager, "addLoadJob", job1);

        //make job1 don't serialize
        Config.streaming_label_keep_max_second = 1;
        Thread.sleep(2000);

        File file = serializeToFile(loadManager);

        LoadManager newLoadManager = deserializeFromFile(file);
        Map<Long, LoadJob> newLoadJobs = Deencapsulation.getField(newLoadManager, fieldName);

        Assert.assertEquals(0, newLoadJobs.size());
    }

    private File serializeToFile(LoadManager loadManager) throws Exception {
        File file = new File("./loadManagerTest");
        file.createNewFile();
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
        loadManager.write(dos);
        dos.flush();
        dos.close();
        return file;
    }

    private LoadManager deserializeFromFile(File file) throws Exception {
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        LoadManager loadManager = new LoadManager(new LoadJobScheduler());
        loadManager.readFields(dis);
        return loadManager;
    }
}
