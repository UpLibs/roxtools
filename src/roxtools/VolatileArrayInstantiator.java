package roxtools;

public interface VolatileArrayInstantiator<E> {

	public int totalInstances() ;
	
	public E instantiate(int index) ;
	
}
