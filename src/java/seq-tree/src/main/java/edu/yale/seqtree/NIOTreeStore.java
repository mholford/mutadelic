package edu.yale.seqtree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NIOTreeStore implements TreeStore<byte[]> {
	String fileName;
	FileChannel inFileChannel;
	FileChannel outFileChannel;
	ByteBuffer buffer;
	long currAddr;
	private FileOutputStream fos;
	private FileInputStream fis;
	private boolean dbg = false;
	private Map<Long, byte[]> map;
	long max = 10000;
	long min = 7500;
	private FileChannel fc;
	private RandomAccessFile raf;
	private boolean newBuild;

	public NIOTreeStore(File f, boolean newBuild) {
		this.newBuild = newBuild;
		fileName = f.getName();
		try {
			if (!(f.exists())) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// fis = new FileInputStream(f);
			// fos = new FileOutputStream(f, !newBuild);
			// inFileChannel = fis.getChannel();
			// outFileChannel = fos.getChannel();
			MapMode mode = newBuild ? MapMode.READ_WRITE : MapMode.READ_ONLY;
			if (newBuild) {
				raf = new RandomAccessFile(f, "rw");
				raf.setLength(200000000);
			} else {
				raf = new RandomAccessFile(f, "r");
			}
			fc = raf.getChannel();
			try {
				buffer = fc.map(mode, 0, fc.size());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map = new HashMap<Long, byte[]>();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currAddr = 0;
	}

	public void close() throws IOException {
		// commit(map.size());
		// inFileChannel.close();
		// outFileChannel.close();
		// fis.close();
		fc.force(true);
		if (newBuild) {
			fc.truncate(currAddr);
		}
		fc.close();
		raf.close();
	}

	public long getSize() throws IOException {
		return fc.size();
	}

	public byte[] retrieveNode(long addr) throws IOException {
		// byte[] b = null;
		// if (!map.containsKey(addr)) {
		// b = retrieveNodeFromFile(addr);
		// putNode(b, addr);
		// } else {
		// b = map.get(addr);
		// }
		byte[] b = retrieveNodeFromFile(addr);

		SeqtreeManager.GETS++;
		return b;
	}

	public byte[] retrieveNodeFromFile(long addr) throws IOException {
		// byte[] b = null;
		// inFileChannel = inFileChannel.position(addr);
		// ByteBuffer buf = ByteBuffer.allocate(SrutiNode.SIZE);
		// inFileChannel.read(buf);
		// b = buf.array();
		// return b;
		byte[] b = new byte[SrutiNode2.SIZE];
		buffer.position((int) addr);
		buffer.get(b, 0, SrutiNode2.SIZE);
		return b;
	}

	public void putNode(byte[] b, long addr) throws IOException {
		// map.put(addr, b);
		// if (map.size() > max) {
		// commit(map.size() - min);
		// }
		writeNodeToFile(b, addr);
		SeqtreeManager.PUTS++;
	}

	public void commit(long num) throws IOException {
		System.out.println(String.format("COMMIT %s", fileName));
		Iterator<Long> keys = map.keySet().iterator();
		int i = 0;
		List<Long> toRemove = new ArrayList<Long>();
		while (i < num) {
			long k = keys.next();
			byte[] b = map.get(k);
			// long addr = SrutiNode.getLongForBytes(b, SrutiNode.ID_PT);
			writeNodeToFile(b, k);
			toRemove.add(k);
			i++;
		}
		for (Long key : toRemove) {
			map.remove(key);
		}
	}

	public void writeNodeToFile(byte[] b, long addr) throws IOException {
		// ByteBuffer buf = ByteBuffer.wrap(b);
		// outFileChannel.write(buf, addr);
		// buf.clear();
		buffer.position((int) addr);
		buffer.put(b, 0, SrutiNode2.SIZE);
	}

	public void putLong(long val, long addr) throws IOException {
		buffer.position((int) addr);
		buffer.putLong(val);
		// outFileChannel.write((ByteBuffer) buf.flip(), addr);
		// buf.clear();
	}

	public void putInt(int val, long addr) throws IOException {
		buffer.position((int) addr);
		buffer.putInt(val);
	}

	public long getLong(long addr) throws IOException {
		// inFileChannel = inFileChannel.position(addr);
		// ByteBuffer buf = ByteBuffer.allocate(8);
		// inFileChannel.read(buf);
		// buf.flip();
		// return buf.getLong();
		if (addr < 0) {
			return -1L;
		}
		buffer.position((int) addr);
		return buffer.getLong();
	}

	public int getInt(long addr) throws IOException {
		if (addr < 0) {
			return -1;
		}
		buffer.position((int) addr);
		return buffer.getInt();
	}

	public long newAddress() {
		long out = currAddr;
		currAddr += SrutiNode2.SIZE;
		return out;
	}

	public long getAddress() {
		return currAddr;
	}

	public void setAddress(long addr) {
		currAddr = addr;
	}

	public ByteBuffer debug(ByteBuffer buf) {
		System.err
				.println(String
						.format(
								"Write: start[%d], end[%d], chr[%d], min[%d], max[%d], left[%d], right[%d], parent[%d], color[%d], ID[%d]",
								buf.getLong(), buf.getLong(), buf.getInt(), buf
										.getLong(), buf.getLong(), buf
										.getLong(), buf.getLong(), buf
										.getLong(), buf.getInt(), buf.getInt()));
		return (ByteBuffer) buf.flip();
	}

	public void postLoad() {
		try {
			commit(map.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
