declare option output:method "xml";
declare option output:indent "yes";

<Users>{
  for $u in /users/row
  order by count(/posts/row[@OwnerUserId = $u/@Id]) descending
  return <User>{
    <DisplayName>{$u/@DisplayName}</DisplayName>,
    <Posts>{count(/posts/row[@OwnerUserId = $u/@Id])}</Posts>
  }</User>
}</Users>
