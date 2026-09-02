// A card reference inside a chat / game-log message. Same markup as every
// other card link in the app (a.card-name[data-card-id]) so the surrounding
// useCardTooltips hook attaches the hover-preview tooltip to it — see
// GlobalChat.tsx / GameChatLog.tsx, which call useCardTooltips on the log
// container.
export function CardToken({
  id,
  name,
  advanced = false,
}: {
  id: string;
  name: string;
  advanced?: boolean;
}) {
  return (
    <a className="card-name text-wrap" data-card-id={id}>
      {name}
      {advanced && <i className="icon adv" />}
    </a>
  );
}
