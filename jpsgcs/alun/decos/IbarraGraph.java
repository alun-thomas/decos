package jpsgcs.alun.decos;

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class IbarraGraph<V> extends Auxiliary<V>
{
	public IbarraGraph(Collection<V> verts)
	{
		super(true);
//System.err.println("IG");

		Set<V> root = new LinkedHashSet<V>();
		for (V v : verts)
		{
			Set<V> C = new LinkedHashSet<V>();
			C.add(v);
			net.connect(root,C);
			map.put(v,C);
		}
	}

	public boolean disconnect(V x, V y, Set<V> Sxy)
	{
		Set<V> Cxy = union(Sxy,x,y);

		if (!net.contains(Cxy))
			return false;
	
		if (!net.contains(Sxy))
			addAbove(Sxy,Cxy);

		Set<V> Cx = union(Sxy,x);
		if (!net.contains(Cx))
			addAbove(Cx,Cxy);

		Set<V> Cy = union(Sxy,y);
		if (!net.contains(Cy))
			addAbove(Cy,Cxy);

		//net.disconnect(Cx,Cxy);
		//net.disconnect(Cy,Cxy);
		net.remove(Cxy);

		Cx = redundant(Cx);
		Cy = redundant(Cy);

		for (V v : Cx)
			map.put(v,Cx);
		for (V v : Cy)
			map.put(v,Cy);

		return true;
	}

	public boolean connect(V x, V y, Set<V> Sxy)
	{
		if (!net.contains(Sxy))
			return false;

		Set<V>[] C = closestSets(x,y,Sxy);
		if (C == null)
			return false;

		Set<V> Sx = union(Sxy,x);
		if (!net.contains(Sx))
			addAbove(Sx,C[0]);

		Set<V> Sy = union(Sxy,y);
		if (!net.contains(Sy))
			addAbove(Sy,C[1]);

		Set<V> Cxy = union(Sxy,x,y);
		net.connect(Sx,Cxy);
		net.connect(Sy,Cxy);

		redundant(Sx);
		redundant(Sy);
		redundant(Sxy);

		for (V v : Cxy)
			map.put(v,Cxy);

		return true;
	}

	protected Set<V>[] closestSets(V x, V y, Set<V> Sxy)
	{
		Set<V> Cx = null;
		Set<V> Cy = map.get(y);

		Map<Set<V>,Set<V>> tree = new LinkedHashMap<Set<V>,Set<V>>();
		tree.put(Cy,null);
		PriorityQueue<Set<V>> q = new PriorityQueue<Set<V>>(new BiggestSet());
		q.add(Cy);

		for (Cx = q.poll(); !Cx.contains(x); Cx = q.poll())
		{
			for (Set<V> next : net.outNeighbours(Cx))
			{
				if (tree.get(next) == null)
				{
					tree.put(next,Cx);
					q.add(next);
				}
			}
			for (Set<V> next : net.inNeighbours(Cx))
			{
				if (tree.get(next) == null)
				{
					tree.put(next,Cx);
					q.add(next);
				}
			}
		}

		boolean good = false;

		for (Cy = Cx; !Cy.contains(y); Cy = tree.get(Cy))
			if (Cy.size() == Sxy.size())
				good = true;

		if (!good)
			return null;

		Set[] res = {Cx, Cy};
		return (Set<V>[])res;
	}

	protected Set<Set<V>> descendants(Set<V> X)
	{
		Set<Set<V>> decs = new LinkedHashSet<Set<V>>();
		decs.add(X);
		Queue<Set<V>> q = new LinkedList<Set<V>>();
		q.add(X);

		for (Set<V> S = q.poll(); S != null; S = q.poll())
		{
			for (Set<V> N : net.outNeighbours(S))
				if (!decs.contains(N))
				{
					decs.add(N);
					q.add(N);
				}
		}

		return decs;
	}

	protected boolean oneDescentComp(Set<V> S)
	{
		Set<Set<V>> decs = new LinkedHashSet<Set<V>>(descendants(S));
		decs.remove(S);

		if (decs.isEmpty())
			return false;

		LinkedList<Set<V>> q = new LinkedList<Set<V>>();
		q.add(decs.iterator().next());

		for (Set<V> C = q.poll(); C != null; C = q.poll())
		{
			decs.remove(C);
			for (Set<V> neib : net.outNeighbours(C))
				if (decs.contains(neib))
					q.add(neib);
			for (Set<V> neib : net.inNeighbours(C))
				if (decs.contains(neib))
					q.add(neib);
		}

		return decs.isEmpty();
	}

	protected Set<V> redundant(Set<V> X)
	{
		if (oneDescentComp(X))
		{
			Set<Set<V>> par = net.inNeighbours(X);
			Set<Set<V>> off = net.outNeighbours(X);
			
			net.remove(X);
	
			for (Set<V> U : par)
				for (Set<V> W : off)
					if (!descendants(U).contains(W))
						net.connect(U,W);

			return off.iterator().next();
		}
		return X;
	}

	protected Set<Set<V>> ancestors(Set<V> X)
	{
		Set<Set<V>> ancs = new LinkedHashSet<Set<V>>();
		ancs.add(X);
		LinkedList<Set<V>> q = new LinkedList<Set<V>>();
		q.add(X);

		for (Set<V> S = q.poll(); S != null; S = q.poll())
		{
			for (Set<V> N : net.inNeighbours(S))
				if (!ancs.contains(N))
				{
					ancs.add(N);
					q.add(N);
				}
		}

		return ancs;
	}

	protected Set<Set<V>> supersets(Set<V> X, Set<V> C)
	{
		Set<Set<V>> sub = new LinkedHashSet<Set<V>>();
		sub.add(C);
		LinkedList<Set<V>> q = new LinkedList<Set<V>>();
		q.add(C);

		for(Set<V> S = q.poll(); S != null; S = q.poll())
		{
			for (Set<V> N : net.inNeighbours(S))
				if (!sub.contains(N) && N.containsAll(X))
				{
					sub.add(N);
					q.add(N);
				}
			for (Set<V> N : net.outNeighbours(S))
				if (!sub.contains(N) && N.containsAll(X))
				{
					sub.add(N);
					q.add(N);
				}
		}

		return sub;
	}

	protected void addAbove(Set<V> X, Set<V> C)
	{
		net.add(X);

		Queue<Set<V>> q = new PriorityQueue<Set<V>>(new BiggestSet());
		q.addAll(ancestors(C));

		for (Set<V> S = q.poll(); S != null; S = q.poll())
		{
			if (X.containsAll(S))
			{
				net.connect(S,X);
				q.removeAll(ancestors(S));
			}
		}

		q = new PriorityQueue<Set<V>>(new SmallestSet());
		q.addAll(supersets(X,C));

		for (Set<V> S = q.poll(); S != null; S = q.poll())
		{
			net.connect(X,S);
			q.removeAll(descendants(S));
		}

		for (Set<V> U : net.inNeighbours(X))
			for (Set<V> W : net.outNeighbours(X))
				net.disconnect(U,W);
	}
}
