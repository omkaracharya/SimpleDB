import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.server.SimpleDB;
import simpledb.tx.recovery.RecoveryMgr;

/**
 * Created by watve on 11/26/2016.
 */
@RunWith(JUnit4.class)
public class ProjectTest {
    @Test
    public void undoRedoTest() {

        //Initialize SimpleDB
        SimpleDB.init("new1");

        //Create 2 Recovery Managers with txids 100 and 200
        RecoveryMgr recoveryMgr1 = new RecoveryMgr(100);
        RecoveryMgr recoveryMgr2 = new RecoveryMgr(200);

        //Create a new block
        Block block = new Block("testblock", 111);

        //Initialize buffer manager and pin block to it
        BufferMgr bufferMgr = SimpleDB.bufferMgr();
        Buffer buffer = bufferMgr.pin(block);

        //Write old values
        buffer.setInt(8, 5, 200, 777);
        buffer.setString(40, "Hello", 200, 888);

        //Write log records explicitly for manager 1
        recoveryMgr1.setInt(buffer, 8, 10);
        recoveryMgr1.commit();

        //Write log records explicitly for manager 2
        recoveryMgr2.setInt(buffer, 8, 10);
        recoveryMgr2.setString(buffer, 40, "World");

        //Recover both transactions
        recoveryMgr1.recover();
        recoveryMgr2.recover();

        //Assert that setInt was redone due to commit record and setString was undone
        assert (buffer.getInt(8) == 10);
        assert ("Hello".equals(buffer.getString(40)));

        System.out.println("Undo Redo test passed.\n");
    }

    @Test
    public void bufferTest() {

        //Initialize SimpleDB
        SimpleDB.init("new2");

        //Create 9 blocks, 8 for pinning, 9th for testing replacement
        Block blk1 = new Block("filename1", 1);
        Block blk2 = new Block("filename2", 2);
        Block blk3 = new Block("filename3", 3);
        Block blk4 = new Block("filename4", 4);
        Block blk5 = new Block("filename5", 5);
        Block blk6 = new Block("filename6", 6);
        Block blk7 = new Block("filename7", 7);
        Block blk8 = new Block("filename8", 8);

        Block blk9 = new Block("filename9", 9);

        //Create Buffer Manager of size 8
        BufferMgr basicBufferMgr = new BufferMgr(8);

        //Pin the 8 blocks, store buffer 2 and 3 for testing
        basicBufferMgr.pin(blk1);
        Buffer buff2 = basicBufferMgr.pin(blk2);
        Buffer buff3 = basicBufferMgr.pin(blk3);
        basicBufferMgr.pin(blk4);
        basicBufferMgr.pin(blk5);
        basicBufferMgr.pin(blk6);
        basicBufferMgr.pin(blk7);
        basicBufferMgr.pin(blk8);

        //Assert number of available blocks is zero
        assert (basicBufferMgr.available() == 0);

        //Unpin block 3 and 2
        basicBufferMgr.unpin(buff3);
        basicBufferMgr.unpin(buff2);

        //Introduce 9th block
        basicBufferMgr.pin(blk9);

        //Assert that the replaced buffer was the same as block2 and not block3
        assert (buff2.equals(basicBufferMgr.getMapping(blk9)));
        assert (!buff3.equals(basicBufferMgr.getMapping(blk9)));

        System.out.println("Buffer FIFO test successful.\n");
    }
}
