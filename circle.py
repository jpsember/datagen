from pycore.base import *

# Hand-written class to investigate how to use Python's properties to better implement
# our generated data classes
#
class Circle(AbstractData):


  # Define serialization keys
  # These are class variables (see https://stackoverflow.com/questions/3434581)
  # and can be accessed from outside e.g. 'print(Circle._1)'
  #
  # These succinct key names have the advantage that we can extract all the keys for a class
  # e.g. by 'vars(Circle)'
  #
  _0 = "radius"
  _1 = "label"


  def __init__(self):
    # Define instance fields, and set them to their default values
    #
    # TODO: the instance field names can be compressed, e.g. '_3', and perhaps the keys could be '_k3' (since
    # they are used less often)
    #
    self._radius = 0
    self._label = None
    self._h = None        # cached value for __hash__ function


  @classmethod
  def new_builder(cls):
    return CircleBuilder()


  def parse(self, obj):
    # TODO: can we construct a builder, use its setters, and call build() instead?  Saves code, but at what cost?
    #
    x = Circle()
    x._radius = obj.get(Circle._0, 0)
    x._label = obj.get(Circle._1, None)
    return x


  def to_builder(self):
    x = CircleBuilder()
    x._radius = self._radius
    x._label = self._label
    return x


  # Should this be renamed to 'serialize'?
  #
  def to_json(self):
    # Pycharm complains that this could be rewritten as a dictionary literal,
    # but we can't handle optional values with that technique.  Disable the warning
    # in the Pycharm preferences.
    #
    x = {}
    x[Circle._0] = self._radius
    if self._label is not None:
      x[Circle._1] = self._label
    return x


  def __hash__(self):
    if self._h is None:
      # TODO: we can optimize to eliminate a couple of lines; i.e. r=<first value>, and last self.hash_value= <calculation>
      x = 1
      x = x * 37 + hash(self._radius)
      if self._label is not None:
        x = x * 37 + hash(self._label)
      self._h = x
    return self._h


  def __eq__(self, other):
    if not isinstance(other, Circle):
      return False
    return hash(self) == hash(other) \
           and self._radius == other.__radius \
           and self._label == other.__label


  # ---------------------------------------------------------------------------------------
  # Properties
  # ---------------------------------------------------------------------------------------


  # Define the (hidden) methods associated with the instance fields' properties
  #
  # TODO: these names could be compressed by using the field names, e.g. _g4(self):
  #
  def _get_radius(self):
    return self._radius


  def _get_label(self):
    return self._label


  # Define the properties representing the instance fields.  Only the builder subclass has setters
  #
  radius = property(_get_radius)
  label = property(_get_label)



# Define a class variable for the default instance
#
Circle.default_instance = Circle()







# Define the builder subclass of this data class
#
class CircleBuilder(Circle):


  # We must override the hash function to discard any previously cached value, as we are a mutable object
  #
  def __hash__(self):
    self._h = None
    return super().__hash__()


  def build(self):
    x = Circle()
    x._radius = self._radius
    x._label = self._label
    return x


  # Define methods unique to the builder; these are public, so they can be called using fluid interface
  #
  def set_radius(self, value):
    self._radius = value; return self
  def set_label(self, value):
    self._label = value; return self


  # Override the property for the builder subclass
  #
  # TODO: IDE is warning that "Setter should not return a value"
  #
  radius = property(Circle._get_radius, set_radius)
  label = property(Circle._get_label, set_label)



#########################################################################################
#
# Reload this script (has some unusual quirks though)
#
def r():
  exec(open('circle.py').read())


# Initialize some things when the script is loaded
#
pr("circle.py loaded")

a = Circle()

# Non-fluidic way:
#
b = CircleBuilder()
b.radius = 8


# Fluidic way:
#
c = b.build().to_builder().set_label("surprise").set_radius(42)
d = c.build()

e = Circle.new_builder()
e.radius = 4

# TODO: IDE is warning of 'unexpected argument' for 7, but the code runs...
# something to do with forward references?
#
e.set_radius(7)

pr(d)
