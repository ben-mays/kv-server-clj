A basic KV server that supports simple exists/equals constraints on update/read. 

`lein ring server-headless` will launch on port 3000.

Example usage:

```
;; Note: It doesn't support multiple value updates, yet.
;; Using JSON to update a key using OCC with a related version key.
{
  "values" : [
    {
      "key" : "my-key",
      "val" : "new-val"
    },
    {
      "key" : "my-version-key",
      "val" : last-version + 1
    }
  ],
  "constraints" : [
    {
      "type" : "equals",
      "key" : "my-version-key",
      "value" : last-version
    }
  ]
}
```
