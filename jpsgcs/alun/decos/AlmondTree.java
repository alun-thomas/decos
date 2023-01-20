package jpsgcs.alun.decos;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;

public class AlmondTree<V> extends Auxiliary<V>
{
	public AlmondTree(Collection<V> verts)
	{
		super(true);
//System.err.println("AT");
		
		Set<V> root = new LinkedHashSet<V>();
		for (V v : verts)
		{
			Set<V> C = new LinkedHashSet<V>();
			C.add(v);
			map.put(v,C);
			net.connect(root,C);
		}
	}

	public boolean connect(V x, V y, Set<V> Sxy)
	{
		if (!net.contains(Sxy))
			return false;

		Set<V>[] res = links(x,map.get(x),Sxy);
		Set<V> Cx = res[0];
		Set<V> Px = res[1]; 
	
		res = links(y,map.get(y),Sxy);
		Set<V> Cy = res[0];
		Set<V> Py = res[1]; 
	
		if (Px.equals(Py))
			return false;
		
		net.disconnect(Sxy,Px);
		net.disconnect(Sxy,Py);
	
		Set<V> Cxy = union(Sxy,x,y);
		net.add(Cxy);

		Set<V> Sx = union(Sxy,x);

		if (net.contains(Sx))
		{
			net.connect(Sx,Cxy);
			checkRedundant(Sx);
		}
		else
		{
			net.connect(Sx,Cx);
			net.connect(Sx,Cxy);
		}

		Set<V> Sy = union(Sxy,y);

		if (net.contains(Sy))
		{
			net.connect(Sy,Cxy);
			checkRedundant(Sy);
		}
		else
		{
			net.connect(Sy,Cy);
			net.connect(Sy,Cxy);
		}

		net.connect(Sxy,Cxy);
		checkRedundant(Sxy);

		for (V v : Cxy)
			map.put(v,Cxy);

		return true;
	}

	public boolean disconnect(V x, V y, Set<V> Sxy)
	{
		Set<V> Cxy = union(Sxy,x,y);

		if (!net.contains(Cxy))
			return false;

		if (net.contains(Sxy))
			net.disconnect(Sxy,links(null,Cxy,Sxy)[1]);

		Set<V> Sx = union(Sxy,x);
		if (net.contains(Sx))
		{
			net.disconnect(Sx,Cxy);
			Sx = checkRedundant(Sx);
		}

		Set<V> Sy = union(Sxy,y);
		if (net.contains(Sy))
		{
			net.disconnect(Sy,Cxy);
			Sy = checkRedundant(Sy);
		} 

		for (Set<V> C : net.inNeighbours(Cxy))
			net.connect(C, (C.contains(x) ? Sx : Sy));
		net.remove(Cxy);

		net.connect(Sxy,Sy);
		net.connect(Sxy,Sx);

		for (V v : Sx)
			map.put(v,Sx);
		for (V v : Sy)
			map.put(v,Sy);

		return true;
	}

	protected Set<V> checkRedundant(Set<V> X)
	{
		if (net.outNeighbours(X).size() == 1)
		{
			Set<V> T = net.outNeighbours(X).iterator().next();
			for (Set<V> C : net.inNeighbours(X))
				net.connect(C,T);
			net.remove(X);
			return T;
		}
		return X;
	}


	private Set<V>[] links(V x, Set<V> Cx, Set<V> S)
	{
		Map<Set<V>,Set<V>> path = new LinkedHashMap<Set<V>,Set<V>>();
		Queue<Set<V>> q = new LinkedList<Set<V>>();
		q.add(Cx);

		for (Set<V> A = q.poll(); !A.equals(S); A = q.poll())
		{
			Set<V> prev = path.get(A);
		
			for (Set<V> next : net.inNeighbours(A))
				if (!next.equals(prev))
				{
					q.add(next);
					path.put(next,A);
				}	
			for (Set<V> next : net.outNeighbours(A))
				if (!next.equals(prev))
				{
					q.add(next);
					path.put(next,A);
				}	
		}

		Set<V> C = Cx;
		if (x != null)
			for (C = S; !C.contains(x); C = path.get(C));

		Set[] res = {C, path.get(S)};
		return (Set<V>[]) res;
	}
}
