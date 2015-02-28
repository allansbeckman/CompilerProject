package types;

import crux.Symbol;

public class IntType extends Type {

    public IntType() {
    }

    @Override
    public String toString() {
        return "int";
    }

    @Override
    public Type add(Type that) {
        if (!(that instanceof IntType))
            return super.add(that);
        return new IntType();
    }

    @Override
    public Type sub(Type that) {
        if (!(that instanceof IntType))
            return super.sub(that);
        return new IntType();
    }

    @Override
    public Type mul(Type that) {
        if (!(that instanceof IntType))
            return super.mul(that);
        return new IntType();
    }

    @Override
    public Type div(Type that) {
        if (!(that instanceof IntType))
            return super.div(that);
        return new IntType();
    }

    @Override
    public Type compare(Type that) {
        if (!(that instanceof IntType))
            return super.compare(that);
        return new BoolType();
    }

	@Override
	public Type assign(Type source) {
		if (!equivalent(source)) {
			return super.assign(source);
		} else {
			return this;
		}
	}

	@Override
    public Type declare(Symbol symbol) {
       	return this;
    }

	@Override
	public Type baseType(Symbol symbol) {
		return this;
	}
    
    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof IntType))
            return false;
        return true;
    }
}
