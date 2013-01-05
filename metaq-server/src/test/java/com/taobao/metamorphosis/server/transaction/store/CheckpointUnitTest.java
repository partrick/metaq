package com.taobao.metamorphosis.server.transaction.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CheckpointUnitTest {

    private Checkpoint checkpoint;
    String path;


    @Before
    public void setUp() throws Exception {
        this.path = System.getProperty("java.io.tmpdir") + File.separator + "checkpoint";
        System.out.println(this.path);
        FileUtils.deleteDirectory(new File(this.path));
        this.checkpoint = new Checkpoint(this.path, 3);
    }


    @After
    public void tearDown() throws IOException {
        this.checkpoint.close();
    }


    @Test
    public void testCheckGetCloseGet() throws Exception {
        assertNull(this.checkpoint.getRecentCheckpoint());
        final JournalLocation location = new JournalLocation(1, 0);
        this.checkpoint.check(location);
        assertEquals(location, this.checkpoint.getRecentCheckpoint());

        // close and reopen
        this.checkpoint.close();
        this.checkpoint = new Checkpoint(this.path, 3);
        assertNotNull(this.checkpoint.getRecentCheckpoint());
        assertEquals(location, this.checkpoint.getRecentCheckpoint());
        assertEquals(1, this.checkpoint.getCheckpoints().size());
    }


    @Test
    public void testCheckEqualOrOlder() throws Exception {
        assertNull(this.checkpoint.getRecentCheckpoint());
        final JournalLocation location = new JournalLocation(1, 0);
        this.checkpoint.check(location);
        assertEquals(location, this.checkpoint.getRecentCheckpoint());
        this.checkpoint.check(location);
        assertEquals(location, this.checkpoint.getRecentCheckpoint());
        this.checkpoint.check(new JournalLocation(0, 0));
        assertEquals(location, this.checkpoint.getRecentCheckpoint());

        // close and reopen
        this.checkpoint.close();
        this.checkpoint = new Checkpoint(this.path, 3);
        assertNotNull(this.checkpoint.getRecentCheckpoint());
        assertEquals(location, this.checkpoint.getRecentCheckpoint());
        assertEquals(1, this.checkpoint.getCheckpoints().size());
    }


    @Test
    public void testCheckCheckGetCloseGet() throws Exception {
        assertNull(this.checkpoint.getRecentCheckpoint());
        final JournalLocation location = new JournalLocation(1, 0);
        this.checkpoint.check(location);
        assertEquals(location, this.checkpoint.getRecentCheckpoint());
        final JournalLocation newLocation = new JournalLocation(1, 1024);
        this.checkpoint.check(newLocation);
        assertEquals(newLocation, this.checkpoint.getRecentCheckpoint());

        // close and reopen
        this.checkpoint.close();
        this.checkpoint = new Checkpoint(this.path, 3);
        assertNotNull(this.checkpoint.getRecentCheckpoint());
        assertEquals(newLocation, this.checkpoint.getRecentCheckpoint());
        assertEquals(2, this.checkpoint.getCheckpoints().size());

    }


    @Test
    public void testCheckRollGetCloseGet() throws Exception {
        assertNull(this.checkpoint.getRecentCheckpoint());
        this.checkpoint.check(new JournalLocation(1, 0));
        assertEquals(new JournalLocation(1, 0), this.checkpoint.getRecentCheckpoint());
        this.checkpoint.check(new JournalLocation(1, 1024));
        assertEquals(new JournalLocation(1, 1024), this.checkpoint.getRecentCheckpoint());
        this.checkpoint.check(new JournalLocation(3, 1024));
        assertEquals(new JournalLocation(3, 1024), this.checkpoint.getRecentCheckpoint());
        this.checkpoint.check(new JournalLocation(3, 2048));
        assertEquals(new JournalLocation(3, 2048), this.checkpoint.getRecentCheckpoint());

        // close and reopen
        this.checkpoint.close();
        this.checkpoint = new Checkpoint(this.path, 3);
        assertNotNull(this.checkpoint.getRecentCheckpoint());
        assertEquals(new JournalLocation(3, 2048), this.checkpoint.getRecentCheckpoint());
        assertEquals(3, this.checkpoint.getCheckpoints().size());
    }
}
