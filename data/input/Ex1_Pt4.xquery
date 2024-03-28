declare option output:method "xml";
declare option output:indent "yes";

<posts>{
  (for $p in /posts/row
  where some $tag in (
    for $t in /tags/row
    return $t/@TagName
  ) [position() le 10] satisfies contains($p/@Tags, $tag)
  return <post>{$p}</post>) [position() le 100]
}</posts>
