package simpledb.tx.recovery;

import simpledb.log.BasicLogRecord;
import simpledb.log.LogIterator;
import simpledb.server.SimpleDB;

import java.util.Iterator;

import static simpledb.tx.recovery.LogRecord.*;

/**
 * A class that provides the ability to read records
 * from the log in reverse order.
 * Unlike the similar class 
 * {@link simpledb.log.LogIterator LogIterator},  
 * this class understands the meaning of the log records.
 * @author Edward Sciore
 */
public class LogRecordIterator implements Iterator<LogRecord> {
    private LogIterator iter = SimpleDB.logMgr().iterator();
   
   public boolean hasNext() {
      return iter.hasNext();
   }

    public boolean hasNextForward() {
        return iter.hasNextForward();
    }
   
   /**
    * Constructs a log record from the values in the 
    * current basic log record.
    * The method first reads an integer, which denotes
    * the type of the log record.  Based on that type,
    * the method calls the appropriate LogRecord constructor
    * to read the remaining values.
    * @return the next log record, or null if no more records
    */
   public LogRecord next() {
      BasicLogRecord rec = iter.next();
      int op = rec.nextInt();
      switch (op) {
         case CHECKPOINT:
            return new CheckpointRecord(rec);
         case START:
            return new StartRecord(rec);
         case COMMIT:
            return new CommitRecord(rec);
         case ROLLBACK:
            return new RollbackRecord(rec);
         case SETINT:
            return new SetIntRecord(rec);
         case SETSTRING:
            return new SetStringRecord(rec);
         default:
            return null;
      }
   }

    public LogRecord nextForward() {
        BasicLogRecord rec = iter.nextForward();
        int op = rec.nextInt();
        switch (op) {
            case CHECKPOINT:
                return new CheckpointRecord(rec);
            case START:
                return new StartRecord(rec);
            case COMMIT:
                return new CommitRecord(rec);
            case ROLLBACK:
                return new RollbackRecord(rec);
            case SETINT:
                return new SetIntRecord(rec);
            case SETSTRING:
                return new SetStringRecord(rec);
            default:
                return null;
        }
    }
   
   public void remove() {
      throw new UnsupportedOperationException();
   }
}