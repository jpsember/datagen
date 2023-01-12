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

fields {
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
