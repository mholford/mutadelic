package edu.yale.dlgen;

public class DLEntity<T> {

	T impl;
	
	public DLEntity() {
	}
	
	public DLEntity(T impl) {
		this.impl = impl;
	}

	public T getImpl() {
		return impl;
	}

	public void setImpl(T impl) {
		this.impl = impl;
	}
	
	public T get() {
		return impl;
	}
	
	public void set(T impl) {
		setImpl(impl);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((impl == null) ? 0 : impl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		DLEntity other = (DLEntity) obj;
		if (impl == null) {
			if (other.impl != null)
				return false;
		} else if (!impl.equals(other.impl))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}
