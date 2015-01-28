package crux;

import static org.junit.Assert.*;

import org.junit.Test;

public class MainTest {

	@Test
	public void test() {
		String[] fileNames = {"tests/test04.crx"};
		 Compiler.main(fileNames);
	}

}
