package simpledb.buffer;

import simpledb.file.Block;
import simpledb.file.FileMgr;

import java.util.*;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * 
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
	private Buffer[] bufferpool;
	//create block to buffer map
	private Map<Block, Buffer> bufferPoolMap;
	private int numAvailable;
	private List<Integer> fifoBufferIndex;

	/**
	 * Creates a buffer manager having the specified number of buffer slots.
	 * This constructor depends on both the {@link FileMgr} and
	 * {@link simpledb.log.LogMgr LogMgr} objects that it gets from the class
	 * {@link simpledb.server.SimpleDB}. Those objects are created during system
	 * initialization. Thus this constructor cannot be called until
	 * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or is called
	 * first.
	 * 
	 * @param numbuffs
	 *            the number of buffer slots to allocate
	 */
	BasicBufferMgr(int numbuffs) {
		bufferpool = new Buffer[numbuffs];
		numAvailable = numbuffs;
		fifoBufferIndex = new ArrayList<Integer>(numbuffs);
		for (int i = 0; i < numbuffs; i++) {
			bufferpool[i] = new Buffer();         
			fifoBufferIndex.add(i);
		}
		bufferPoolMap = new HashMap<Block, Buffer>();
	}

	/**
	 * Flushes the dirty buffers modified by the specified transaction.
	 * 
	 * @param txnum
	 *            the transaction's id number
	 */
	synchronized void flushAll(int txnum) {
		for (Buffer buff : bufferpool)
			if (buff.isModifiedBy(txnum))
				buff.flush();
	}

	/**
	 * Pins a buffer to the specified block. If there is already a buffer
	 * assigned to that block then that buffer is used; otherwise, an unpinned
	 * buffer from the pool is chosen. Returns a null value if there are no
	 * available buffers.
	 * 
	 * @param blk
	 *            a reference to a disk block
	 * @return the pinned buffer
	 */
	synchronized Buffer pin(Block blk) {
		Buffer buff = findExistingBuffer(blk);
		if (buff == null) {
			buff = chooseUnpinnedBuffer();
			if (buff == null)
				return null;
			//remove if block already exists in map
			bufferPoolMap.remove(buff.block());
			buff.assignToBlock(blk);
			//add block to buffer mapping
			bufferPoolMap.put(blk, buff);
		}
		if (!buff.isPinned())
			numAvailable--;
		buff.pin();
		return buff;
	}

	/**
	 * Allocates a new block in the specified file, and pins a buffer to it.
	 * Returns null (without allocating the block) if there are no available
	 * buffers.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param fmtr
	 *            a pageformatter object, used to format the new block
	 * @return the pinned buffer
	 */
	synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
		Buffer buff = chooseUnpinnedBuffer();
		if (buff == null)
			return null;
		//remove if block already exists in map
		bufferPoolMap.remove(buff.block());
		buff.assignToNew(filename, fmtr);
		numAvailable--;
		buff.pin();
		//add block to buffer mapping
		bufferPoolMap.put(buff.block(), buff);

		return buff;
	}

	/**
	 * Unpins the specified buffer.
	 * 
	 * @param buff
	 *            the buffer to be unpinned
	 */
	synchronized void unpin(Buffer buff) {
		buff.unpin();
		if (!buff.isPinned())
			numAvailable++;
	}

	/**
	 * Returns the number of available (i.e. unpinned) buffers.
	 * 
	 * @return the number of available buffers
	 */
	int available() {
		return numAvailable;
	}

	private Buffer findExistingBuffer(Block blk) {
		//if buffer is assigned to block, lookup map to get the assigned buffer.
		if (containsMapping(blk)) {
			Buffer buff = bufferPoolMap.get(blk);
			return buff;
		} 
		return null;
	}

	private Buffer chooseUnpinnedBuffer() {
		//FIFO buffer replacement policy
		  Iterator<Integer> iter = fifoBufferIndex.iterator();
		  int id;
		  while ( iter.hasNext() ) {
			 id = iter.next();
			 if ( !bufferpool[id].isPinned()) {
				 iter.remove();
				 fifoBufferIndex.add(id);
				 return bufferpool[id];
			 }
		  }
		return null;
	}

	/**
	 * Determines whether the map has a mapping from the block to some buffer.
	 * 
	 * @paramblk the block to use as a key
	 * @return true if there is a mapping; false otherwise
	 */
	boolean containsMapping(Block blk) {
		//check if mapping exists for the current block
		return bufferPoolMap.containsKey(blk);
	}

	/**
	 * Returns the buffer that the map maps the specified block to.
	 * 
	 * @paramblk the block to use as a key
	 * @return the buffer mapped to if there is a mapping; null otherwise
	 */
	Buffer getMapping(Block blk) {
		return bufferPoolMap.get(blk);
	}
}
