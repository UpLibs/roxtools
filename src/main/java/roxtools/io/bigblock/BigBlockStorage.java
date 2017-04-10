package roxtools.io.bigblock;

import java.io.IOException;
import java.io.OutputStream;

public interface BigBlockStorage {

	public OutputStream openBigBlockOutput(String name, int blockPart) throws IOException ;

	public void closeBigBlockOutput(String name, int blockPart, OutputStream blockOutput) throws IOException ;

	public void storeIndex(String name, byte[] serial) throws IOException ;

	public byte[] readStoredBlockIndex(String name) throws IOException;

	public byte[] getEntryDataStored(String name, BigBlockEntry blockEntry) throws IOException;

}
