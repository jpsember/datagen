#!/usr/bin/env python3

import random
from pycore.base import *
from gen.strlist import *

# Verify that defensive copies are made of lists by builders
#
x = Strlist.new_builder()
x.set_ints([1,2,3]);
x = x.build()

y = x.to_builder()
y.strs = ["new y str"]
y.ints_def = [88,88]

z = y.build().to_builder()
z.ints_def = None

pr("x:", x)
pr("y:", y)
pr("z:", z)