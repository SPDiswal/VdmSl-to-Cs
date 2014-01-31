
public class SetUtil
{
	public static VDMSet set()
	{
		return new VDMSet();
	}
	
	public static VDMSet set(Object... elements)
	{
		if(elements == null)
			throw new IllegalArgumentException("Cannot instantiate set from null");
		
		VDMSet set = set();
		
		for(Object element : elements)
			set.add(element);
		
		return set;
	}
	
	public static VDMSet union(VDMSet left, VDMSet right)
	{
		if(left == null || right == null)
			throw new IllegalArgumentException("Cannot union null");

		VDMSet result = new VDMSet();
		
		result.addAll(left);
		result.addAll(right);
		
		return result;
	}
	
	public static VDMSet diff(VDMSet left, VDMSet right)
	{
		if(left == null || right == null)
			throw new IllegalArgumentException("Cannot get set difference of null");

		VDMSet result = new VDMSet();
		
		result.addAll(left);
		result.removeAll(right);
		
		return result;
	}
	
	public static boolean psubset(VDMSet left, VDMSet right)
	{
		if(left == null || right == null)
			throw new IllegalArgumentException("proper subset is undefined for null");

		return left.size() < right.size() && right.containsAll(left);
	}
	
	public static VDMSet intersect(VDMSet left, VDMSet right)
	{
		if(left == null || right == null)
			throw new IllegalArgumentException("Cannot intersect null");

		VDMSet result = new VDMSet();
		
		result.addAll(left);
		result.retainAll(right);
		
		return result;
	}
	
	public static boolean equals(VDMSet left, VDMSet right)
	{
		if(left == null || right == null)
			throw new IllegalArgumentException("A set cannot be compared to null");
		
		if(left.size() != right.size())
			return false;
		
		return left.containsAll(right);
	}
}