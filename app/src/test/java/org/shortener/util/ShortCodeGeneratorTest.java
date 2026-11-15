package org.shortener.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

  @Test
  void generatesCodeOfConfiguredLength() {
    ShortCodeGenerator generator = new ShortCodeGenerator(code -> false, 6);
    String code = generator.generateUniqueCode();

    assertNotNull(code);
    assertEquals(6, code.length());
  }

  @Test
  void generatesUniqueCodesInBulk() {
    ShortCodeGenerator generator = new ShortCodeGenerator(code -> false, 6);
    Set<String> codes = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      codes.add(generator.generateUniqueCode());
    }
    assertEquals(1000, codes.size());
  }

  @Test
  void generatedCodeIsAlphanumeric() {
    ShortCodeGenerator generator = new ShortCodeGenerator(code -> false, 6);
    for (int i = 0; i < 100; i++) {
      String code = generator.generateUniqueCode();
      assertTrue(code.matches("[A-Za-z0-9]+"), "Unexpected code: " + code);
    }
  }

  @Test
  void skipsAlreadyExistingCodes() {
    Set<String> taken = new HashSet<>();
    ShortCodeGenerator generator = new ShortCodeGenerator(taken::contains, 6);
    String first = generator.generateUniqueCode();
    taken.add(first);

    for (int i = 0; i < 50; i++) {
      String next = generator.generateUniqueCode();
      assertNotEquals(first, next);
      taken.add(next);
    }
  }
}
