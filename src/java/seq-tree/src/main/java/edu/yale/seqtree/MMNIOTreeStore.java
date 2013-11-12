package edu.yale.seqtree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class MMNIOTreeStore implements TreeStore<ByteBuffer> {
	FileChannel inFileChannel;
	FileChannel outFileChannel;
	MappedByteBuffer inBuffer;
	MappedByteBuffer outBuffer;
	long currAddr;
	private FileOutputStream fos;
	private FileInputStream fis;
	private boolean dbg = false;

	public MMNIOTreeStore(File f, boolean newBuild) {
		try {
			if (!(f.exists())) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fis = new FileInputStream(f);
			fos = new FileOutputStream(f, !newBuild);
			inFileChannel = fis.getChannel();
			outFileChannel = fos.getChannel();
			try {
				inBuffer = inFileChannel.map(MapMode.READ_ONLY, 0, 1000000000);
				outBuffer = outFileChannel.map(MapMode.READ_WRITE, 0,
						1000000000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		currAddr = 0;
	}

	public void close() throws IOException {
		inFileChannel.close();
		outFileChannel.close();
		fis.close();
		fos.close();
	}

	public long getSize() throws IOException {
		return inFileChannel.size();
	}

	public ByteBuffer retrieveNode(long addr) throws IOException {
		byte[] b = new byte[SrutiNode2.SIZE];
		inBuffer.get(b, (int) addr, b.length);
		ByteBuffer buf = ByteBuffer.wrap(b);
		return (ByteBuffer) buf.flip();
	}

	public void putNode(ByteBuffer buf, long addr) throws IOException {
		if (dbg) {
			buf = debug(buf);
		}
		outBuffer.put(buf.array(), (int) addr, buf.array().length);
		buf.clear();
	}

	public void putInt(int val, long addr) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.rewind();
		buf.putInt(val);
		outBuffer.put(buf.array(), (int) addr, buf.array().length);
		buf.clear();
	}

	public void putLong(long val, long addr) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(val);
		outBuffer.put(buf.array(), (int) addr, buf.array().length);
		buf.clear();
	}

	public long getLong(long addr) throws IOException {
		byte[] b = new byte[8];
		inBuffer.get(b, (int) addr, b.length);
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.flip();
		return buf.getLong();
	}

	public long newAddress() {
		long out = currAddr;
		currAddr += SrutiNode2.SIZE;
		return out;
	}

	public ByteBuffer debug(ByteBuffer buf) {
		System.err.println(String.format(
				"Write: start[%d], end[%d], chr[%d], min[%d], max[%d], "
						+ "left[%d], right[%d], parent[%d], color[%d], ID[%d]",
				buf.getLong(), buf.getLong(), buf.getInt(), buf.getLong(),
				buf.getLong(), buf.getLong(), buf.getLong(), buf.getLong(),
				buf.getInt(), buf.getInt()));
		return (ByteBuffer) buf.flip();
	}

	@Override
	public long getAddress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(long addr) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAddress(long addr) {
		// TODO Auto-generated method stub

	}
}
