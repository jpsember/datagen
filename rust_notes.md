# Rust modules

This describes how Rust modules can be organized in a heirarchy.


As a concrete example, suppose a project’s `src` directory contains:
```
src
├── main.rs
└── echo
    ├── mod.rs
    ├── alpha.rs
    ├── bravo.rs
    └── charlie
         ├── mod.rs
         └── delta.rs
```
(Note that files such as `Cargo.toml` are omitted for clarity.)

Observe that in addition to the *crate root*, `main.rs`, we have three source files, `alpha.rs`, `bravo.rs`, and `delta.rs`, in a subtree that starts at `echo`.

There are two things to consider:

1. Declaring these source files as modules.
2. Importing these modules into a program.

The purpose of the first step is to define the module hierarchy for the Rust compiler.  While this seems apparent to us from the directory structure, we need to use `mod.rs` files to explicitly inform the compiler of the structure.

The purpose of the second step is to allow rust code to use the various structs and functions defined in the modules.

## Placing `mod.rs` files

We can think of the `echo` subdirectory as a subtree.  The subtree root node has three child nodes: `alpha.rs`, `bravo.rs`, and the subdirectory `charlie`.  We construct `echo/mod.rs` with this content:

```
pub mod alpha;
pub mod bravo;
pub mod charlie;
```

We also create `echo/charlie/mod.rs` with this content:
```
pub mod delta;
```

## Accessing modules within a program

Within `main.rs`, we include these statements:

```
mod echo;

use echo::alpha::*;
use echo::bravo::*;
use echo::charlie::delta::*;
```

The `mod` statement above connects `main.rs` to the `mod.rs` files within the `echo` tree.  The `use` statements allow the `main.rs` program to access the Rust elements defined in the `echo` subtree.
