(ns io.github.rutledgepaulv.newrelic-clj.types
  (:import (clojure.lang IFn)
           (com.newrelic.api.agent Trace)))



(deftype TransactionFn [f]
  IFn
  (^{Trace {:dispatcher true}} invoke [_]
    (f))
  (^{Trace {:dispatcher true}} invoke [_ arg1]
    (f arg1))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2]
    (f arg1 arg2))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3]
    (f arg1 arg2 arg3))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4]
    (f arg1 arg2 arg3 arg4))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5]
    (f arg1 arg2 arg3 arg4 arg5))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6]
    (f arg1 arg2 arg3 arg4 arg5 arg6))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (^{Trace {:dispatcher true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 args]
    (apply arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 args))
  (^{Trace {:dispatcher true}} applyTo [_ args] (apply f args)))

(deftype AsyncTransactionFn [f]
  IFn
  (^{Trace {:async true}} invoke [_]
    (f))
  (^{Trace {:async true}} invoke [_ arg1]
    (f arg1))
  (^{Trace {:async true}} invoke [_ arg1 arg2]
    (f arg1 arg2))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3]
    (f arg1 arg2 arg3))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4]
    (f arg1 arg2 arg3 arg4))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5]
    (f arg1 arg2 arg3 arg4 arg5))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6]
    (f arg1 arg2 arg3 arg4 arg5 arg6))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20]
    (f arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (^{Trace {:async true}} invoke [_ arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 args]
    (apply arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 args))
  (^{Trace {:async true}} applyTo [_ args] (apply f args)))

