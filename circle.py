#!/usr/bin/env python3
from pycore.base import *

# Hand-written class to investigate how to use Python's properties to better implement
# our generated data classes
#
class Circle(AbstractData):

  # Define serialization keys
  # TODO: what are these? global variables?  Class variables?  Can't seem to print them
  #
  _0 = "radius"
  _1 = "label"

  # Declare default value for this data class; declare it here, initialize it later
  #
  # TODO: does this declare a 'class variable' (i.e. singleton) as we want, as opposed to an instance field?
  #
  default_instance = None

  def __init__(self):
    # Define instance fields, and set them to their default values
    #
    self._radius = 0
    self._label = None


  @classmethod
  def new_builder(cls):
    return CircleBuilder()


  def parse(self, obj):
    # TODO: can we construct a builder, use its setters, and call build() instead?  Saves code, but at what cost?
    #
    inst = Circle()
    inst._radius = obj.get(Circle._0, 0)
    inst._label = obj.get(Circle._1, None)
    return inst


  def to_builder(self):
    x = CircleBuilder()
    x._radius = self._radius
    x._label = self._label
    return x


  # Should this be renamed to 'serialize'?
  #
  def to_json(self):
    # TODO: can we rewrite this as a dict literal to avoid the IDE warning
    #  "This dictionary creation could be rewritten as a dictionary literal"?
    m = {}
    m[Circle._0] = self._radius
    if self._label is not None:
      m[Circle._1] = self._label
    return m


  def __hash__(self):
    if self._hash_value is None:
      # TODO: we can optimize to eliminate a couple of lines; i.e. r=<first value>, and last self.hash_value= <calculation>
      r = 1
      r = r * 37 + hash(self._radius)
      if self._label is not None:
        r = r * 37 + hash(self._label)
      self._hash_value = r
    return self._hash_value

  def __eq__(self, other):
    if isinstance(other, Circle):
      return hash(self) == hash(other) \
             and self._radius == other.__radius \
             and self._label == other.__label
    else:
      return False

  # ---------------------------------------------------------------------------------------
  # Properties
  # ---------------------------------------------------------------------------------------

  # Define the (hidden) methods associated with the instance fields' properties
  #
  def _get_radius(self):
    return self._radius

  def _get_label(self):
    return self._label

  # Define the properties representing the instance fields.  Only the builder subclass has setters
  #
  # TODO: rename _get, _set to be as short as possible
  #
  radius = property(_get_radius)
  label = property(_get_label)




# Initialize the default value
# TODO: can this not be done earlier?  Or, its declaration moved here?
#
Circle.default_instance = Circle()







# Define the builder subclass of this data class
#
class CircleBuilder(Circle):


  def build(self):
    v = Circle()
    v._radius = self._radius
    v._label = self._label
    return v


  # Define (hidden) methods unique to the builder;
  # can these be public, so they can be called using fluid interface?
  #
  def set_radius(self, value):
    self._radius = value; return self
  def set_label(self, value):
    self._label = value; return self

  # Redefine the property for the builder class
  # TODO: is this appropriate?  Or are there now two 'radius' properties, one in the parent class?
  #
  # TODO: IDE is warning that "Setter should not return a value"
  #
  radius = property(Circle._get_radius, set_radius)
  label = property(Circle._get_label, set_label)


def r():
  exec(open('circle.py').read())


print("circle.py loaded")
a = Circle()

# NOTE: we are giving up some fluid-like capability (b.setXXX(...).setYY(...)...);
# but I have made the setters return 'self', to it is now fluidic once again
#
b = CircleBuilder()
b.radius = 8


c = b.build().to_builder().set_label("surprise").set_radius(42)
d = c.build()

pr(d)
