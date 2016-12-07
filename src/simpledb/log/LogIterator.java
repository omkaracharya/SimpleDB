package simpledb.log;

import simpledb.file.Block;
import simpledb.file.Page;
import simpledb.server.SimpleDB;

import java.util.Iterator;

import static simpledb.file.Page.INT_SIZE;

/**
 * A class that provides the ability to move through the
 * records of the log file in reverse order.
 *
 * @author Edward Sciore
 */
public class LogIterator implements Iterator<BasicLogRecord> {
    //PTR_SIZE is sum of size of both forward and previous pointers
    public static final int PTR_SIZE = 2 * INT_SIZE;
    private Block blk;
    private Page pg = new Page();
    private int currentrec;

    /**
     * Creates an iterator for the records in the log file,
     * positioned after the last log record.
     * This constructor is called exclusively by
     * {@link LogMgr#iterator()}.
     */
    LogIterator(Block blk) {
        this.blk = blk;
        pg.read(blk);
        //creates log iterator with current rec pointing to end of file.
        currentrec = pg.getInt(LogMgr.LAST_POS);
    }

    /**
     * Determines if the current log record
     * is the earliest record in the log file.
     *
     * @return true if there is an earlier record
     */
    public boolean hasNext() {
        return currentrec > 0 || blk.number() > 0;
    }

    /**
     * Moves to the next log record in reverse order.
     * If the current log record is the earliest in its block,
     * then the method moves to the next oldest block,
     * and returns the log record from there.
     *
     * @return the next earliest log record
     */
    public BasicLogRecord next() {
        if (currentrec == 0)
            moveToNextBlock();
        currentrec = pg.getInt(currentrec);
        return new BasicLogRecord(pg, currentrec + PTR_SIZE);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Moves to the next log block in reverse order,
     * and positions it after the last record in that block.
     */
    private void moveToNextBlock() {
        blk = new Block(blk.fileName(), blk.number() - 1);
        pg.read(blk);
        currentrec = pg.getInt(LogMgr.LAST_POS);
    }

    /**
     * Moves to the next log record in forward order.
     * If the current log record is the latest in its block,
     * then the method moves to the next block,
     * and returns the log record from there.
     *
     * @return the next log record
     */
    public BasicLogRecord nextForward() {
        //if current record is the last record of block, move to next block
        if (currentrec == pg.getInt(LogMgr.LAST_POS))
            moveToNextForwardBlock();
        //set current record position to next record position
        currentrec = pg.getInt(currentrec + INT_SIZE);
        //read current record
        return new BasicLogRecord(pg, pg.getInt(currentrec) + PTR_SIZE);
    }

    /**
     * Moves to the next log block in forward order,
     * and positions it at the first record in that block.
     */
    private void moveToNextForwardBlock() {
        //move to next block
        blk = new Block(blk.fileName(), blk.number() + 1);
        pg.read(blk);
        //set current record to start of file.
        currentrec = LogMgr.LAST_POS;
    }

    /**
     * Determines if the current log record
     * is the latest record in the log file.
     *
     * @return true if there is a later record
     */
    public boolean hasNextForward() {
        //check if there is a another record in the block/ or there exists next block.
        return currentrec < pg.getInt(LogMgr.LAST_POS) || blk.number() < SimpleDB.fileMgr().size(blk.fileName()) - 1;
    }
}
