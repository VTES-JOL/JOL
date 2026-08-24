import type { ReactNode } from 'react';
import { childrenOfType } from './mdxChildren';

export interface CommandOptionProps {
  name: string;
  children: ReactNode;
}

// Marker only, read by CommandDoc — see CommandDoc's doc comment.
export function CommandOption(_props: CommandOptionProps) {
  return null;
}

export interface CommandExampleProps {
  cmd: string;
  children: ReactNode;
}

// Marker only, read by CommandDoc — see CommandDoc's doc comment.
export function CommandExample(_props: CommandExampleProps) {
  return null;
}

// Renders a command's syntax string (e.g. "play [SOURCE] [@ DISCIPLINES]
// [DESTINATION] [draw]") as literal words plus pill badges for each
// [PLACEHOLDER] token, matching how DoCommand.java's argument names read.
// Tokenizes on whole [...] groups first — some placeholders contain a space
// (e.g. "[AMOUNT / withdraw]", "[PLAYER / burn]") and a plain
// `syntax.split(' ')` would otherwise cut those in half.
function tokenizeSyntax(syntax: string): string[] {
  return syntax.match(/\[[^\]]*\]|\S+/g) ?? [];
}

function SyntaxLine({ syntax }: { syntax: string }) {
  return (
    <div className="help-command-syntax">
      {tokenizeSyntax(syntax).map((token, i) => {
        const isPlaceholder = token.startsWith('[') && token.endsWith(']');
        return isPlaceholder ? (
          <span key={i} className="badge rounded-pill text-bg-secondary fw-normal help-command-placeholder">
            {token.slice(1, -1)}
          </span>
        ) : (
          <span key={i}>{token}</span>
        );
      })}
    </div>
  );
}

export interface CommandDocProps {
  name: string;
  syntax: string;
  description: ReactNode;
  children?: ReactNode; // nested <CommandOption>/<CommandExample>
}

// The single reference layout for one command: a syntax line with inline
// placeholder badges, a definition-style option list, and an example list —
// replaces the old JSP's per-command nested-card/table blocks with one
// purpose-built component content authors reuse via MDX.
export function CommandDoc({ name, syntax, description, children }: CommandDocProps) {
  const options = childrenOfType(children, CommandOption);
  const examples = childrenOfType(children, CommandExample);

  return (
    <section className="help-command my-4">
      <h3 className="h5 mb-2">{name}</h3>
      <SyntaxLine syntax={syntax} />
      <p className="text-body-secondary mt-2 mb-3">{description}</p>
      {options.length > 0 && (
        <div className="mb-3">
          <div className="help-command-heading">Options</div>
          {options.map((option, i) => (
            <div key={i} className="help-command-row">
              <span className="help-command-row-label">{option.props.name}</span>
              <span className="small">{option.props.children}</span>
            </div>
          ))}
        </div>
      )}
      {examples.length > 0 && (
        <div>
          <div className="help-command-heading">Examples</div>
          {examples.map((example, i) => (
            <div key={i} className="help-command-row">
              <code className="help-command-row-label">{example.props.cmd}</code>
              <span className="small text-body-secondary">{example.props.children}</span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
