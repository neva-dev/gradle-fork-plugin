/*
 * File copied and adapted from Java to Kotlin language.
 * Original source available at: https://github.com/hsz/idea-gitignore/blob/master/src/mobi/hsz/idea/gitignore/util/Glob.java
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 hsz Jakub Chrzanowski <jakub@hsz.mobi>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.neva.gradle.fork.file.filter

import java.util.regex.Pattern

object GitIgnore {

  fun globToPattern(input: String): Pattern {
    return Pattern.compile(globToRegexString(input))
  }

  fun globToRegex(input: String): Regex {
    return globToRegexString(input).toRegex()
  }

  fun globToRegexString(input: String, acceptChildren: Boolean = true): String {
    var glob = input
    glob = glob.trim { it <= ' ' }

    val sb = StringBuilder("^")
    var escape = false
    var star = false
    var doubleStar = false
    var bracket = false
    var beginIndex = 0

    if (glob.startsWith("**")) {
      sb.append("(?:[^/]*?/)*")
      beginIndex = 2
      doubleStar = true
    } else if (glob.startsWith("*/")) {
      sb.append("[^/]*")
      beginIndex = 1
      star = true
    } else if (glob == "*") {
      sb.append(".*")
    } else if (glob.startsWith('*')) {
      sb.append(".*?")
    } else if (glob.startsWith('/')) {
      beginIndex = 1
    } else {
      val slashes = glob.count { it == '/' }
      if (slashes == 0 || slashes == 1 && glob.endsWith('/')) {
        sb.append("(?:[^/]*?/)*")
      }
    }

    val chars = glob.substring(beginIndex).toCharArray()
    for (ch in chars) {
      if (bracket && ch != ']') {
        sb.append(ch)
        continue
      } else if (doubleStar) {
        doubleStar = false
        if (ch == '/') {
          sb.append("(?:[^/]*/)*?")
          continue
        } else {
          sb.append("[^/]*?")
        }
      }

      if (ch == '*') {
        if (escape) {
          sb.append("\\*")
          escape = false
          star = false
        } else if (star) {
          val prev = if (sb.isNotEmpty()) sb[sb.length - 1] else '\u0000'
          if (prev == '\u0000' || prev == '^' || prev == '/') {
            doubleStar = true
          } else {
            sb.append("[^/]*?")
          }
          star = false
        } else {
          star = true
        }
        continue
      } else if (star) {
        sb.append("[^/]*?")
        star = false
      }

      when (ch) {

        '\\' -> if (escape) {
          sb.append("\\\\")
          escape = false
        } else {
          escape = true
        }

        '?' -> if (escape) {
          sb.append("\\?")
          escape = false
        } else {
          sb.append('.')
        }

        '[' -> {
          if (escape) {
            sb.append('\\')
            escape = false
          } else {
            bracket = true
          }
          sb.append(ch)
        }

        ']' -> {
          if (!bracket) {
            sb.append('\\')
          }
          sb.append(ch)
          bracket = false
          escape = false
        }

        '.', '(', ')', '+', '|', '^', '$', '@', '%' -> {
          sb.append('\\')
          sb.append(ch)
          escape = false
        }

        else -> {
          escape = false
          sb.append(ch)
        }
      }
    }

    if (star || doubleStar) {
      if (sb.endsWith('/')) {
        sb.append(if (acceptChildren) ".+" else "[^/]+/?")
      } else {
        sb.append("[^/]*/?")
      }
    } else {
      if (sb.endsWith('/')) {
        if (acceptChildren) {
          sb.append("[^/]*")
        }
      } else {
        sb.append(if (acceptChildren) "(?:/.*)?" else "/?")
      }
    }

    sb.append('$')

    return sb.toString()
  }

}
