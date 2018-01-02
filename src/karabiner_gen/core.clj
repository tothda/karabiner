(ns karabiner-gen.core
  (:require [cheshire.core :refer [generate-string]])
  (:refer-clojure :exclude [pop]))

(def config
  {:global   {"check_for_updates_on_startup"  true
              "show_in_menu_bar"              true,
              "show_profile_name_in_menu_bar" false}
   :profiles []})

(def capsvim-profile
  {:name                  "capsvim2"
   :virtual_hid_keyboard  {:caps_lock_delay_milliseconds 0
                           :keyboard_type                "ansi"}
   :simple_modifications  [{:from {:key_code "caps_lock"}
                            :to   {:key_code "left_control"}}]
   :complex_modifications {:parameters {}
                           :rules      [{:description  "bla bla"
                                         :manipulators [{:type "basic"
                                                         :from {:key_code  "h"
                                                                :modifiers {:mandatory "left_control"}}
                                                         :to   [{:key_code "left_arrow"}]}
                                                        {:type "basic"
                                                         :from {:key_code  "j"
                                                                :modifiers {:mandatory "left_control"}}
                                                         :to   [{:key_code "down_arrow"}]}
                                                        {:type "basic"
                                                         :from {:key_code  "k"
                                                                :modifiers {:mandatory "left_control"}}
                                                         :to   [{:key_code "up_arrow"}]}
                                                        {:type "basic"
                                                         :from {:key_code  "l"
                                                                :modifiers {:mandatory "left_control"}}
                                                         :to   [{:key_code "right_arrow"}]}]}]}})

(defn layer-factory
  "Creates a function which is capable of defining manipulators that inherit the conditions from
  the layer factory."
  [conditions]
  (let [conditions (for [[cond name val] conditions]
                     {:type  (if (= cond :if) "variable_if" "variable_unless")
                      :name  name
                      :value val})]
    (fn layer-factory'
      ([from] (layer-factory' from from nil))
      ([from to] (layer-factory' from to nil))
      ([from to modifiers]
       (let [modifiers (cond
                         (nil? modifiers) []
                         (coll? modifiers) modifiers
                         :else [modifiers])]
         {:type       "basic"
          :conditions conditions
          :from       {:key_code from
                       ;:modifiers {:optional ["any"]}
                       }
          :to         [{:key_code  to
                        :modifiers modifiers}]})))))

(def snap (layer-factory [[:if "snap" 1] [:if "snap-d" 0] [:if "snap-f" 0] [:if "pop" 0]]))
(def snap-d (layer-factory [[:if "snap-d" 1] [:if "pop" 0]]))
(def snap-f (layer-factory [[:if "snap-f" 1] [:if "pop" 0]]))
(def pop (layer-factory [[:if "snap" 0] [:if "pop" 1]]))
(def standard (layer-factory [[:if "snap" 0] [:if "pop" 0]]))

(def crackle-profile
  {:name                  "crackle"
   :selected              true
   :virtual_hid_keyboard  {:caps_lock_delay_milliseconds 0
                           :keyboard_type                "ansi"}
   :complex_modifications {:parameters {}
                           :rules      [{:description  "bla bla"
                                         :manipulators [
                                                        ; snap on
                                                        {:type            "basic"
                                                         :conditions      [{:type "variable_if" :name "snap" :value 0}
                                                                           {:type "variable_if" :name "pop" :value 0}]
                                                         :from            {:key_code  "caps_lock"
                                                                           :modifiers {:optional ["any"]}}
                                                         :to              [{:set_variable {:name "snap" :value 1}}]
                                                         :to_after_key_up [{:set_variable {:name "snap" :value 0}}]}
                                                        {:type            "basic"
                                                         :conditions      [{:type "variable_if" :name "snap" :value 0}
                                                                           {:type "variable_if" :name "pop" :value 0}]
                                                         :from            {:key_code  "quote"
                                                                           :modifiers {:optional ["any"]}}
                                                         :to              [{:set_variable {:name "snap" :value 1}}]
                                                         :to_after_key_up [{:set_variable {:name "snap" :value 0}}]}
                                                        ; pop on
                                                        {:type            "basic"
                                                         :conditions      [{:type "variable_if" :name "snap" :value 0}
                                                                           {:type "variable_if" :name "pop" :value 0}]
                                                         :from            {:key_code  "open_bracket"
                                                                           :modifiers {:optional ["any"]}}
                                                         :to              [{:set_variable {:name "pop" :value 1}}]
                                                         :to_after_key_up [{:set_variable {:name "pop" :value 0}}]}

                                                        ; SNAP-D
                                                        {:type            "basic"
                                                         :conditions      [{:type "variable_if" :name "snap" :value 1}
                                                                           {:type "variable_if" :name "snap-f" :value 0}
                                                                           {:type "variable_if" :name "pop" :value 0}]
                                                         :from            {:key_code  "d"
                                                                           :modifiers {:optional ["any"]}}
                                                         :to              [{:set_variable {:name "snap-d" :value 1}}]
                                                         :to_if_alone     [{:key_code "hyphen"}]
                                                         :to_after_key_up [{:set_variable {:name "snap-d" :value 0}}]}

                                                        ; SNAP-F
                                                        {:type            "basic"
                                                         :conditions      [{:type "variable_if" :name "snap" :value 1}
                                                                           {:type "variable_if" :name "snap-d" :value 0}
                                                                           {:type "variable_if" :name "pop" :value 0}]
                                                         :from            {:key_code  "f"
                                                                           :modifiers {:optional ["any"]}}
                                                         :to              [{:set_variable {:name "snap-f" :value 1}}]
                                                         :to_if_alone     [{:key_code "quote" :modifiers "shift"}]
                                                         :to_after_key_up [{:set_variable {:name "snap-f" :value 0}}]}

                                                        ; changes in standard mode
                                                        (standard "tab" "left_control")
                                                        (standard "semicolon" "return_or_enter")
                                                        (standard "comma" "delete_or_backspace")

                                                        ; snap layer
                                                        ; top row
                                                        (snap "tab" "hyphen" "shift")
                                                        (snap "q" "1")
                                                        (snap "w" "2")
                                                        (snap "e" "3")
                                                        (snap "r" "4")
                                                        (snap "t" "5")
                                                        (snap "y" "6")
                                                        (snap "u" "7")
                                                        (snap "i" "8")
                                                        (snap "o" "9")
                                                        (snap "p" "0")
                                                        (snap "open_bracket" "backslash" "shift")
                                                        (snap "close_bracket" "grave_accent_and_tilde")

                                                        ; middle row
                                                        (snap "semicolon" "slash" "shift")
                                                        (snap "quote" "semicolon")
                                                        (snap "l" "right_arrow")
                                                        (snap "k" "up_arrow")
                                                        (snap "j" "down_arrow")
                                                        (snap "h" "left_arrow")
                                                        (snap "g" "quote")
                                                        ;(snap "f" "quote" "shift")
                                                        ;(snap "d" "hyphen")
                                                        (snap "s" "equal_sign" "shift")
                                                        (snap "a" "equal_sign")
                                                        (snap "caps_lock" "semicolon" "shift")

                                                        ; bottom row
                                                        (snap "right_shift" "escape")
                                                        (snap "slash" "period" "shift")
                                                        (snap "period" "period")
                                                        (snap "comma" "comma")
                                                        (snap "m" "grave_accent_and_tilde" "shift")
                                                        (snap "n" "backslash")
                                                        (snap "b" "slash")
                                                        (snap "v" "9" "shift")
                                                        (snap "c" "open_bracket")
                                                        (snap "x" "open_bracket" "shift")
                                                        (snap "z" "comma" "shift")
                                                        (snap "left_shift" "tab")

                                                        (snap-d "h" "h" ["command" "option" "control" "shift"]) ; load file in repl
                                                        (snap-d "j" "j" ["command" "option" "control" "shift"]) ; eval top form
                                                        (snap-d "k" "k" ["command" "option" "control" "shift"]) ; clear repl
                                                        (snap-d "l" "l" ["command" "option" "control" "shift"]) ; slurp forward
                                                        (snap-d "p" "p" ["command" "option" "control" "shift"]) ; run tests in current ns
                                                        (snap-d "o" "o" ["command" "option" "control" "shift"]) ; run test under caret
                                                        (snap-d "i" "i" ["command" "option" "control" "shift"]) ; raise sexp
                                                        (snap-d "m" "m" ["command" "option" "control" "shift"]) ; splice sexp

                                                        ; selection, copy-paste, undo, redo
                                                        (snap-f "l" "right_arrow" "shift")
                                                        (snap-f "k" "up_arrow" "shift")
                                                        (snap-f "j" "down_arrow" "shift")
                                                        (snap-f "h" "left_arrow" "shift")
                                                        (snap-f "i" "i" ["option" "control" "shift"])
                                                        (snap-f "b" "v" "command") ; paste
                                                        (snap-f "n" "c" "command") ; copy
                                                        (snap-f "m" "x" "command") ; cut
                                                        (snap-f "u" "z" "command") ; undo
                                                        (snap-f "y" "z" ["shift" "command"]) ; redo

                                                        ; pop
                                                        (pop "d" "open_bracket" ["command" "shift"]) ; select previous tab
                                                        (pop "f" "close_bracket" ["command" "shift"]) ; select next tab
                                                        (pop "j" "open_bracket" ["command"]) ; select next tab
                                                        (pop "k" "close_bracket" ["command"]) ; select next tab
                                                        (pop "q" "1" ["shift"]) ; !
                                                        (pop "w" "2" ["shift"]) ; @
                                                        (pop "e" "3" ["shift"]) ; #
                                                        (pop "r" "4" ["shift"]) ; $
                                                        (pop "t" "5" ["shift"]) ; %
                                                        (pop "y" "6" ["shift"]) ; ^
                                                        (pop "u" "7" ["shift"]) ; &
                                                        (pop "i" "8" ["shift"]) ; *
                                                        (pop "c" "close_bracket") ; ]
                                                        (pop "v" "0" ["shift"]) ; )
                                                        (pop "x" "close_bracket" ["shift"]) ; }
                                                        ]}]}})



(spit "/Users/david/.config/karabiner/karabiner.json"
      (generate-string
        (-> config
            (update :profiles conj capsvim-profile)
            (update :profiles conj crackle-profile))
        {:pretty true}))
