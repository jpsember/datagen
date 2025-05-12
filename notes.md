## data types


+ string
+ boxedInteger
+ JSMap
+ JSList

Primitive versions:

+ primitiveInteger

Types that implement the AbstractData interface:

+ Foo

### enumerated types

Both defining a type, and referring to one

Can an enumerated type be treated just as another type, e.g. string?

### Optional values

`
? string name;
`

Values can be null *if and only if* they are optional.


### structured types

+ array: `* string names;`

+ map:  has two associated types, one for keys, one for labels

+ set:  `set string names;`

Structured types can also be optional, e.g. `? *string names`, `? map string integer freq`


### Optimizations for certain primitive types

+ array of primitive integers: `* int foo`


## Program options (configuration)

From `datagen_config.dat`:

```
enum Language;

class {
  File start_dir;
  File dat_path = "dat_files";

  // If defined, converts from old .proto format to .dat format
  //
  File proto_path;

  Language language;
  File source_path;
  bool clean;
  bool delete_old;
  bool treat_warnings_as_errors;

  // Include extended comments in generated source files
  //
  bool comments;

  // For Python, if nonempty, used instead of source_path
  //
  File python_source_path;
}
```

## Fields and immutability

We think of the 'built' data type instances as being immutable, or in other words, as having immutable fields - a caller cannot change a field's value.

In practice, this has some gotchas:

+ Primitive arrays
  ```
  int[] a;
  ```
  The reference `a` cannot be changed (it cannot be made to point to a different array), but its elements *can* be changed.

+ Lists, Maps, Sets

  Consider a list:
  ```
  List<String> a;
  ```
  The reference `a` cannot be changed, but the size and contents of `a` *can* be changed. Maps and sets have the same quality as lists here.

+ Java Objects
  ```
  Foo a;
  ```
  The reference `a` cannot be changed; but fields of `a` can be changed if class `Foo` has methods (or public fields) that allow this.

We support a `debug` mode, where the code is modified to try to catch errors in client code.  Errors that it will attempt to catch include:

+ getting a list L from an immutable data object X, then later modifying L (and modifying X unintentionally)
+ passing a list L as an argument to a builder Y's setter, and later modifying L (unintentionally modifying Y)

I don't see a practical way to enforce these constraints (i.e. throwing an exception if they are violated), so I may abandon further work on the `debug` mode.

