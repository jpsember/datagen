#!/usr/bin/env python3
from pycore.base import *

# Hand-written class to investigate how to use Python's properties to better implement
# our generated data classes
#
class Circle(AbstractData):

  # Define serialization keys
  #
  _key_radius = "radius"

  # Declare default value for this data class; declare it here, initialize it later
  #
  # TODO: does this declare a 'class variable' (i.e. singleton) as we want, as opposed to an instance field?
  #
  default_instance = None

  def __init__(self):
    # Define instance fields, and set them to their default values
    #
    self._radius = 0


  @classmethod
  def new_builder(cls):
    return CircleBuilder()


  def parse(self, obj):
    # TODO: can we construct a builder, use its setters, and call build() instead?  Saves code, but at what cost?
    #
    inst = Circle()
    inst._radius = obj.get(Circle._key_radius, 0)
    return inst

  # Define the (hidden) methods associated with the instance fields' properties
  #
  def _get_radius(self):
    return self._radius

  # Define the properties representing the instance fields.  Only the builder subclass has setters
  #
  radius = property(
    fget=_get_radius
  )

  def to_builder(self):
    x = CircleBuilder()
    x._radius = self._radius
    return x


  # Should this be renamed to 'serialize'?
  #
  def to_json(self):
    # TODO: can we rewrite this as a dict literal to avoid the IDE warning
    #  "This dictionary creation could be rewritten as a dictionary literal"?
    m = {}
    m[Circle._key_radius] = self._radius
    return m


  def __hash__(self):
    if self._hash_value is None:
      # TODO: we can optimize to eliminate a couple of lines; i.e. r=<first value>, and last self.hash_value= <calculation>
      r = 1
      r = r * 37 + hash(self._radius)
      self._hash_value = r
    return self._hash_value

  def __eq__(self, other):
    if isinstance(other, Circle):
      return hash(self) == hash(other) \
             and self._radius == other.__radius
    else:
      return False


# Initialize the default value
# TODO: can this not be done earlier?  Or, its declaration moved here?
#
Circle.default_instance = Circle()
























# Define the builder subclass of this data class
#
class CircleBuilder(Circle):

  # Define (hidden) methods unique to the builder
  #
  def _set_radius(self, value):
    self._radius = value

  # Redefine the property for the builder class
  # TODO: is this appropriate?  Or are there now two 'radius' properties, one in the parent class?
  #
  radius = property(
    fget=Circle._get_radius,
    fset=_set_radius
  )


def r():
  exec(open('circle.py').read())


print("circle.py loaded")
a = Circle()
b = CircleBuilder()
