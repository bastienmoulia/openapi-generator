# NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
# https://openapi-generator.tech
# Do not edit the class manually.

defmodule OpenapiPetstore.Model.Animal do
  @moduledoc """
  
  """

  @derive [Poison.Encoder]
  defstruct [
    :"className",
    :"color"
  ]

  @type t :: %__MODULE__{
    :"className" => String.t,
    :"color" => String.t
  }
end

defimpl Poison.Decoder, for: OpenapiPetstore.Model.Animal do
  def decode(value, _options) do
    value
  end
end
