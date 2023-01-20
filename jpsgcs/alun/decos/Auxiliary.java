package jpsgcs.alun.decos;

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;

import jpsgcs.alun.graph.Network;
import jpsgcs.alun.graph.Graph;

abstract public class Auxiliary<V>
{
	protected Network<Set<V>,Set<V>> net = null;
	protected Map<V,Set<V>> map = null;

	public Auxiliary(boolean directed)
	{
		net = new Network(directed);
		map = new LinkedHashMap<V,Set<V>>();
	}
	
	abstract public boolean connect(V x, V y, Set<V> S);
	abstract public boolean disconnect(V x, V y, Set<V> C);

	public Graph getGraph()
	{
		return net;
	}

	public void pause()
	{
		try { System.in.read(); } catch (Exception e) { }
	}

	public Set<V> commonNeighbours(V x, V y)
	{
		return null;
	}

	protected Set<V> intersection(Set<V> A, Set<V> B)
	{
		Set<V> S = new LinkedHashSet<V>(A);
		S.retainAll(B);
		return S;
	}

	protected Set<V> union(Set<V> A, V x)
	{
		Set<V> S = new LinkedHashSet<V>(A);
		S.add(x);
		return S;
	}
		
	protected Set<V> union(Set<V> A, V x, V y)
	{
		Set<V> S = new LinkedHashSet<V>(A);
		S.add(x);
		S.add(y);
		return S;
	}
}
