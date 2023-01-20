package jpsgcs.alun.decos;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;

import jpsgcs.alun.graph.Network;
import jpsgcs.alun.graph.Graph;

public class JunctionTree<V> extends Auxiliary<V>
{
	public JunctionTree(Collection<V> verts)
	{
		super(false);
//System.err.println("JT");

		Set<V> root = null;
		for (V v : verts)
		{
			Set<V> C = new LinkedHashSet<V>();
			C.add(v);
			map.put(v,C);
			net.add(C);

			if (root == null)
				root = C;
			else
				net.connect(root,C,new LinkedHashSet<V>());
		}
	}

	public boolean disconnect(V x, V y, Set<V> Sxy)
	{
		Set<V> Cxy = union(Sxy,x,y);

		if (!net.contains(Cxy))
			return false;

		Set<V> Cx = null;
		for (Set<V> C : net.getNeighbours(Cxy))
			if (C.contains(x) && C.containsAll(Sxy))
			{
				Cx = C;
				net.disconnect(Cx,Cxy);
				break;
			}

		if (Cx == null)
			Cx = union(Sxy,x);

		Set<V> Cy = null;
		for (Set<V> C : net.getNeighbours(Cxy))
			if (C.contains(y) && C.containsAll(Sxy))
			{
				Cy = C;
				net.disconnect(Cy,Cxy);
				break;
			}

		if (Cy == null)
			Cy = union(Sxy,y);

		net.connect(Cx,Cy);

		for (Set<V> C : net.getNeighbours(Cxy))
			if (C.contains(x))
				net.connect(C,Cx);	
			else
				net.connect(C,Cy);
		net.remove(Cxy);

		for (V v : Cx)
			map.put(v,Cx);
		for (V v : Cy)
			map.put(v,Cy);
		
		return true;
	}

	public boolean connect(V x, V y, Set<V> Sxy)
	{
		Set<V> Cy = map.get(y);
		Set<V> Cx = null;

		Map<Set<V>,Set<V>> tree = new LinkedHashMap<Set<V>,Set<V>>();
		Queue<Set<V>> q = new LinkedList<Set<V>>();
		
		for (Cx = Cy; !Cx.contains(x); Cx = q.poll())
		{
			Set<V> prev = tree.get(Cx);
			for (Set<V> next : net.getNeighbours(Cx))
				if (next != prev)
				{
					q.add(next);
					tree.put(next,Cx);
				}
		}

		boolean good = false;

		for (Cy = Cx; !Cy.contains(y); Cy = tree.get(Cy))
		{
			if (!good)
			{
				if (intersection(Cy,tree.get(Cy)).size() == Sxy.size())
				{
					net.disconnect(Cy,tree.get(Cy));
					good = true;
				}
			}
		}

		if (!good)
			return false;

		Set<V> Cxy = union(Sxy,x,y);
		net.add(Cxy);

		if (Cx.size() < Cxy.size())
		{
			for (Set<V> A : net.getNeighbours(Cx))
				net.connect(A,Cxy);
			net.remove(Cx);
		}
		else
			net.connect(Cx,Cxy);
	
		if (Cy.size() < Cxy.size())
		{
			for (Set<V> A : net.getNeighbours(Cy))
				net.connect(A,Cxy);
			net.remove(Cy);
		}
		else
			net.connect(Cy,Cxy);

		for (V v : Cxy)
			map.put(v,Cxy);

		return true;
	}

	public Set<V> commonNeighbours(V x, V y)
	{
		Set<V> Cx = null;
		Set<V> Cy = map.get(y);
		
		Map<Set<V>,Set<V>> tree = new LinkedHashMap<Set<V>,Set<V>>();
		Queue<Set<V>> q = new LinkedList<Set<V>>();
		
		for (Cx = Cy; !Cx.contains(x); Cx = q.poll())
		{
			Set<V> prev = tree.get(Cx);
			for (Set<V> next : net.getNeighbours(Cx))
				if (next != prev)
				{
					q.add(next);
					tree.put(next,Cx);
				}
		}

		if (Cx.contains(y))
		{
			Set<V> S = new LinkedHashSet<V>();

			Set<Set<V>> seen = new LinkedHashSet<Set<V>>();
			seen.add(Cx);
			q.clear();
			q.add(Cx);
			for (Cy = q.poll(); Cy != null; Cy = q.poll())
			{
				S.addAll(Cy);
				for (Set<V> C : net.getNeighbours(Cy))
					if (!seen.contains(C) && C.contains(x) && C.contains(y))
					{
						seen.add(C);
						q.add(C);
					}
			}

			S.remove(x);
			S.remove(y);
			return S;
		}
		else
		{
			for (Cy = Cx; !Cy.contains(y); Cy = tree.get(Cy));
			return intersection(Cx,Cy);
		}
	}
}
