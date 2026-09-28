"Explain Diamond Problem in Java", answer:

"The Diamond Problem occurs in multiple inheritance when a child class inherits the same method through two different parent classes, creating ambiguity about which implementation to use. Java avoids this by not supporting multiple inheritance with classes. However, since Java 8 interfaces can have default methods, similar conflicts can occur with multiple interfaces. In that case, the implementing class must override the conflicting method and can explicitly call InterfaceName.super.method() to choose a particular implementation."

Remember these 3 things:

Diamond Problem =

🔺 Two inheritance paths
⚠️ Same method / ambiguity
✅ Child resolves it

And the Java-specific rule:

Multiple classes ❌ — Multiple interfaces ✅ — Default-method conflict → override and resolve.

If you remember "Two parents, same method → child must decide",
==============================================================


Majorly based on lambda streams possible 

operators  intermediate & Terinaery operators   flow is 
intermediate operators (are lazy operators)
======================
1)filter
2)map
3)flatmap
4)findFirst
5)findany
6)finadall


Terinary operators
------------------
1)foreach
2)Collect
3)reduce
4)toList
5)toMap
6)toSet
