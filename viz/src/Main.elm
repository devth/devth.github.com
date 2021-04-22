module Main exposing (Model, Msg(..), init, main, update, view)

import Browser
import Html exposing (Html, button, div, text)
import Html.Events exposing (onClick)


main =
    Browser.sandbox { init = init, update = update, view = view }



-- MODEL


type alias Model =
    { glucose : Float
    , glucagon : Float
    , insulin : Float
    }


emptyModel : Model
emptyModel =
    { glucose = 0.0
    , glucagon = 0.0
    , insulin = 0.0
    }


init : Model
init =
    emptyModel



-- UPDATE


type Msg
    = Increment
    | Decrement


update : Msg -> Model -> Model
update msg model =
    model



-- case msg of
--     Increment ->
--         model + 1
--     Decrement ->
--         model - 1
-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ button [ onClick Decrement ] [ text "-" ]
        , div [] [ text (String.fromFloat model.glucose) ]
        , button [ onClick Increment ] [ text "+" ]
        ]
